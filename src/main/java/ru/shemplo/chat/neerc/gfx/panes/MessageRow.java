package ru.shemplo.chat.neerc.gfx.panes;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.time.format.DateTimeFormatter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import ru.shemplo.chat.neerc.config.SharedContext;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.gfx.scenes.SceneListener;

public class MessageRow extends HBox {
    
    private static final DateTimeFormatter DATE_FORMAT 
          = DateTimeFormatter.ofPattern ("HH:mm:ss");
    private final MessageInterpreter messageInterpreter;
    private SharedContext sharedContext;
    
    public MessageRow (SceneListener listener, MessageEntity message) {
        try {
            this.sharedContext = WindowManager.getInstance ()
                               . getSharedContext ();
        } catch (InterruptedException ie) {}
        this.messageInterpreter = sharedContext
                                . getMessageInterpreter ();
        
        setPadding (new Insets (4)); setSpacing (8);
        getStyleClass ().add ("message-row");
        
        Label date = new Label (message.getTime ().format (DATE_FORMAT));
        date.getStyleClass ().add ("message-date");
        getChildren ().add (date);
        
        final String author = message.getAuthor ();
        Label source = new Label (author);
        if (sharedContext.getUsersService ().isPower (author)) {
            source.setGraphic (messageInterpreter.getIcon ("admin", 16));
            source.setGraphicTextGap (8.0);
        }
        source.setTextFill   (sharedContext.getUsersService ()
                              . getColorForName (author));
        source.getStyleClass  ().add ("message-source");
        source.setAlignment   (Pos.CENTER_RIGHT);
        getChildren ().addAll (source, messageInterpreter
                                       . interpret (message));
        
        setOnMouseClicked (me -> {
            if (me.getClickCount () != 2) { return; }
            final String value = message.getBody ();
            
            StringSelection string = new StringSelection  (value);
            Toolkit.getDefaultToolkit ().getSystemClipboard ()
            . setContents (string, string);
        });
    }
    
}
