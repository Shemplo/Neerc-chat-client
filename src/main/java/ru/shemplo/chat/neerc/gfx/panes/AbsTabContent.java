package ru.shemplo.chat.neerc.gfx.panes;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;
import static ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess.*;

import java.time.LocalDateTime;
import java.util.Objects;

import org.jivesoftware.smack.util.StringUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.gfx.scenes.MainSceneHolder;
import ru.shemplo.chat.neerc.gfx.scenes.SceneComponent;

public abstract class AbsTabContent extends VBox {
    
    private static final KeyCodeCombination SEND_TRIGGER 
          = new KeyCodeCombination (ENTER, SHORTCUT_DOWN);
    
    @Getter protected final boolean sendingMessageEnable;
    @Getter protected MessageAccess access = PUBLIC;
    @Getter protected final MainSceneHolder holder;
    @Getter @Setter protected String input = "";
    @Getter protected final String dialog;
    
    protected HBox toolbar = new HBox (8.0);
    
    public AbsTabContent (boolean sendingMessage, MainSceneHolder holder, String dialog) {
        this.sendingMessageEnable = sendingMessage;
        this.holder = holder; this.dialog = dialog;
        
        toolbar.setAlignment (Pos.CENTER_LEFT);
        toolbar.setPadding (new Insets (8.0));
        getChildren ().add (toolbar);
        
        Button toWindow = new Button ();
        toWindow.setGraphic (holder.getManager ().getSharedContext ()
                                   . getMessageInterpreter ()
                                   . getIcon ("windows", 16, 16));
        toolbar.getChildren ().add (toWindow);
        
        VBox stub = new VBox ();
        VBox.setVgrow (stub, Priority.ALWAYS);
        getChildren ().add (stub);
        
        if (sendingMessageEnable) {
            WindowManager.loadComponent ("tab_input_area")
                         .ifPresent (input -> {
                getChildren ().add (input);
                
                final TextArea area = SceneComponent.INPUT.get (input);
                if (area != null) {
                    area.addEventHandler (KeyEvent.KEY_PRESSED, ke -> {
                        if (!SEND_TRIGGER.match (ke)) { return; }
                        readAndSendIfPossible (area);
                    });                    
                }
                
                final MessageInterpreter interpreter 
                    = holder.getManager ().getSharedContext ()
                    . getMessageInterpreter ();
                
                SceneComponent.SEND.<Button> safe (input).ifPresent (send -> {
                    send.setGraphic (interpreter.getIcon ("send", 36, 20));
                    send.setOnAction (__ -> readAndSendIfPossible (area));
                    send.setBackground (Background.EMPTY);
                    send.setCursor (Cursor.HAND);
                });
                
                SceneComponent.SMILE.<Button> safe (input).ifPresent (smile -> {
                    smile.setGraphic (interpreter.getIcon ("smile", 24, 24));
                    smile.setBackground (Background.EMPTY);
                    smile.setCursor (Cursor.HAND);
                    smile.setDisable (true);
                });
            });
        }
    }
    
    protected void setContent (Node content) {
        Objects.requireNonNull (content);
        getChildren ().set (1, content);
    }
    
    private void readAndSendIfPossible (TextArea input) {
        if (input == null || !sendingMessageEnable) { return; }
        
        if (input.getText ().trim ().length () == 0) { return; }
        sendMessage (input.getText ());
        input.setText ("");
    }
    
    private void sendMessage (String body) {
        ClientAdapter adapter = holder.getManager ()
                              . getSharedContext ()
                              . getClientAdapter ();
        ConfigStorage storage = holder.getManager ()
                              . getConfigStorage ();
        final String id     = StringUtils.randomString (32),
                     author = storage.get ("login")
                            . orElse ("[user name]");
        final LocalDateTime time = LocalDateTime.now ();
        final String recipient = access.equals (PUBLIC) ? "" : dialog;
        
        MessageEntity message = new MessageEntity (dialog, id, 
                       time, author, recipient, body, access);
        adapter.sendMessage (message);
    }
    
    public abstract void onResponsibleTabOpened (Tab owner);
    
}
