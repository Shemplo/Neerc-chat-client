package ru.shemplo.chat.neerc.gfx.panes;

import static ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess;
import ru.shemplo.chat.neerc.gfx.scenes.MainSceneHolder;
import ru.shemplo.chat.neerc.gfx.scenes.SceneHolder;
import ru.shemplo.chat.neerc.network.MessageService;
import ru.shemplo.chat.neerc.network.listeners.MessageListener;

public class Conversation extends AbsTabContent implements MessageListener {
    
    protected final AtomicInteger unread;
    
    protected final Set <String> bufferIDs = new HashSet <> ();
    protected final ListView <MessageEntity> messagesView;
    protected final ObservableList <MessageEntity> buffer;
    protected final MessageService messageService;
    protected final MainSceneHolder listener;
    
    public Conversation (SceneHolder listener, String dialog) {
        super (true, dialog);
        
        this.buffer = FXCollections.observableArrayList ();
        this.listener = (MainSceneHolder) listener;
        this.unread = new AtomicInteger (0);
        
        if (!dialog.equals ("public") && !dialog.equals ("tasks")) {
            HBox toolbar = new HBox ();
            toolbar.setAlignment (Pos.CENTER_LEFT);
            toolbar.setPadding (new Insets (8.0));
            getChildren ().add (toolbar);
            
            Label labelAboutType = new Label ("Send as: ");
            toolbar.getChildren ().add (labelAboutType);
            
            final boolean isUser   = listener.getManager ().getUsersService ().isUser (dialog),
                          isPublic = dialog.equals ("public");
            final MessageAccess access = isUser ? PRIVATE : (isPublic ? PUBLIC : ROOM_PRIVATE);
            
            ChoiceBox <String> dialogType = new ChoiceBox <> ();
            dialogType.setItems (
                    FXCollections.observableArrayList (
                            Arrays.asList (MessageAccess.values ()).stream ()
                            . map     (MessageAccess::getDisplayName)
                            . collect (Collectors.toList ())
                        )
                    );
            dialogType.getSelectionModel ().selectedIndexProperty ()
                    .addListener ((all, down, up) -> {
                this.access = MessageAccess.values () [up.intValue ()];
            });
            dialogType.getSelectionModel ().clearAndSelect (access.ordinal ());
            toolbar.getChildren ().add (dialogType);
        }
        
        this.messagesView = new ListView <> ();
        messagesView.setCellFactory (__ -> new MessageCell ());
        messagesView.setBackground (Background.EMPTY);
        VBox.setVgrow (messagesView, Priority.ALWAYS);
        messagesView.editableProperty ().set (false);
        getChildren ().add (messagesView);
        messagesView.setItems (buffer);
        
        this.messageService = listener
                            . getManager        ()
                            . getSharedContext  ()
                            . getMessageHistory ();
        messageService.subscribe (this);
    }
    
    @Override
    public void onResponsibleTabOpened (Tab owner) {
        if (unread.get () > 0) {
            Platform.runLater (() -> owner.setText (dialog));
            final LocalDateTime now = LocalDateTime.now ();
            messageService.markAsReadUntil (dialog, now);
            unread.set (0);
        }
    }
    
    @Override
    public boolean onAdded (MessageEntity message) {
        if (!message.getDialog ().equals (dialog)) { return false; }
        
        Platform.runLater (() -> {
            if (!bufferIDs.contains (message.getID ())) {
                bufferIDs.add (message.getID ());
                buffer.add (message);
                
                if (!listener.getCurrentConversation ()
                             .equals (this)) {
                    unread.incrementAndGet ();
                }
            }
            
            if (unread.get () > 0) {
                final Tab owner = listener.getOrCreateAndGetTabFor (dialog, this);
                owner.setText (String.format ("%s (%d)", dialog, unread.get ()));
            }
        });
        
        return true;
    }
    
    @Override
    public boolean onDeleted (String id) {
        return true;
    }

    @Override
    public boolean onEdited (String id) {
        messagesView.refresh ();
        return true;
    }
    
}
