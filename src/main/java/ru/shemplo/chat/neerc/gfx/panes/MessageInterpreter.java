package ru.shemplo.chat.neerc.gfx.panes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import afester.javafx.svg.SvgLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.PresenceMessageEntity;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class MessageInterpreter {
    
    private UsersService usersService;
    
    public final Pane interpret (MessageEntity message) {
        VBox lines = new VBox ();
        HBox.setHgrow (lines, Priority.ALWAYS);
        
        if (message instanceof PresenceMessageEntity) {
            PresenceMessageEntity entity = (PresenceMessageEntity) message;
            TextFlow flow = new TextFlow ();
            flow.setLineSpacing (4.0);
            
            _makePressenceMessageBody (flow, entity);
            lines.getChildren ().add  (flow);
        } else { _makeDefaultMessage (lines, message); }
        
        return lines;
    }
    
    private Label makeIconLabel (String emoji) {
        Group icon = getIconWithHeight ("emoji/" + emoji, 20);
        Label tmp = new Label ();
        tmp.setPadding (new Insets (0, 0, 0, 4));
        if (icon == null) {
            tmp.setText (":" + emoji + ":");
        } else {
            tmp.setGraphic (icon);
        }
        
        return tmp;
    }
    
    public Group getIcon (String iconName) {
        String path = String.format ("/gfx/%s.svg", iconName);
        try (
            InputStream is = getClass ().getResourceAsStream (path);
        ) {
            if (is == null) { return null; }
            return new SvgLoader ().loadSvg (is);
        } catch (IOException ioe) {}
        
        return null;
    }
    
    public Group getIcon (String iconName, double width, double height) {
        return Optional.ofNullable (getIcon (iconName)).map (icon -> {
            Bounds bounds = icon.getBoundsInLocal ();
            icon.setScaleX (width  / bounds.getWidth ());
            icon.setScaleY (height / bounds.getHeight ());
            
            return new Group (icon);
        }).orElse (null);
    }
    
    
    public Group getIcon (String iconName, double size) {
        return getIcon (iconName, size, size);
    }
    
    public Group getIconWithHeight (String iconName, double height) {
        return Optional.ofNullable (getIcon (iconName)).map (icon -> {
            Bounds bounds = icon.getBoundsInLocal ();
            double mod = height / bounds.getHeight ();
            icon.setScaleX (mod); icon.setScaleY (mod);
            
            return new Group (icon);
        }).orElse (null);
    }
    
    private void _makePressenceMessageBody (Pane container, PresenceMessageEntity message) {
        final String strStatus = message.getStatus ().name ().toLowerCase ();
        //container.setSpacing (8);
        //container.setHgap (8);
        
        final String name = message.getUser ();
        Label user = new Label (name);
        user.setTextFill (usersService.getColorForName (name));
        user.setPadding (new Insets (0, 4, 0, 4));
        if (usersService.isPower (name)) {
            user.setGraphic (getIcon ("admin", 16));
            user.setGraphicTextGap (8.0);
        }
        container.getChildren ().add (user);
        
        Label middle = new Label (String.format ("is %s", strStatus));
        middle.setPadding (new Insets (0, 8, 0, 4));
        container.getChildren ().add (middle);
        
        ImageView icon = new ImageView ("/gfx/user-" + strStatus + ".png");
        container.getChildren ().add (icon);   
    }
    
    private void _makeDefaultMessage (Pane container, MessageEntity message) {
        String tmpBody = message.getBody (), ident = "(\\w|_|-)+";
        final Pattern emojiPattern 
            = Pattern.compile (String.format (":%s:", ident));
        final Insets insets = new Insets (0, 8, 0, 0);
        
        int number = 0;
        if (tmpBody.length () == 0) { return; } // message is empty
        while (number < tmpBody.length () && tmpBody.charAt (number) == '!') { 
            number += 1; 
        }
        
        String tmpApplyingStyleClass = "";
        if (number > 0) {
            tmpApplyingStyleClass = "red-message";
            tmpBody = tmpBody.substring (number);
        }
        
        final String applyingStyleClass = tmpApplyingStyleClass,
                     body               = tmpBody;
        final double fontSizeAddition   = number + (number > 0 ? 3 : 0);
        
        Arrays.asList (body.split ("\\n")).forEach (line -> {
            TextFlow box = new TextFlow ();
            box.setLineSpacing (4.0);
            
            List <String> tokens = new ArrayList <> ();
            int last = 0;
            
            Matcher matcher = emojiPattern.matcher (line);
            while (matcher.find ()) {
                final String find = matcher.group ();
                if (last != matcher.start ()) {
                    tokens.add (body.substring (last, matcher.start ()));
                }
                
                last = matcher.end ();
                tokens.add (find);
            }
            if (last != line.length ()) {
                tokens.add (line.substring (last));
            }
            
            tokens.forEach (token -> {
                if (token.matches (emojiPattern.pattern ())) {
                    token = token.substring (1, token.length () - 1);
                    Label label = makeIconLabel (token);
                    label.setPadding (insets);
                    
                    box.getChildren ().add (label);
                } else {
                    Arrays.asList (token.split ("\\s")).forEach (word -> {
                        Label label = new Label (word);
                        if (fontSizeAddition > 0) {
                            double fontSize = label.getFont ().getSize () + fontSizeAddition;
                            label.setStyle (String.format ("-fx-font-size: %.0f", fontSize));                            
                        }
                        
                        label.getStyleClass ().add (applyingStyleClass);
                        label.setPadding (insets);
                        
                        box.getChildren ().add (label);
                    });
                }
            });
            
            container.getChildren ().add (box);
        });
    }
    
}
