package ru.shemplo.chat.neerc.gfx.scenes;

import static java.time.ZoneOffset.*;
import static ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.panemu.tiwulfx.control.DetachableTab;
import com.panemu.tiwulfx.control.DetachableTabPane;
import org.jivesoftware.smack.util.StringUtils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import lombok.Getter;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.gfx.panes.*;
import ru.shemplo.chat.neerc.network.exten.ClockExtension.ClockStatus;
import ru.shemplo.chat.neerc.network.exten.editor.EditMessageExtension;
import ru.shemplo.chat.neerc.network.exten.editor.EditMessageExtension.EditActionType;
import ru.shemplo.chat.neerc.network.listeners.ConnectionStatusListener;
import ru.shemplo.snowball.stuctures.Trio;

public class MainSceneHolder extends AbsSceneHolder implements ConnectionStatusListener {
    
    @Getter private AbsTabContent currentConversation;
    
    private final Map <String, Conversation> knownConversations = new ConcurrentHashMap <> ();
    private final MainSceneListener sceneListener = new MainSceneListener (this);
    private final Map <String, Tab> openedTabs = new ConcurrentHashMap <> ();
    
    protected MainSceneHolder (WindowManager manager, Scene scene) {
        super (manager, scene);
        
        ScrollPane usersScroll = SceneComponent.USERS_SCROLL.get (scene);
        usersScroll.setBackground (Background.EMPTY);
        usersScroll.setBorder (Border.EMPTY);
        usersScroll.setOnScroll (se -> {
            double hValue = usersScroll.getHvalue (), delta = se.getDeltaY ();
            HBox users = SceneComponent.USERS.get (scene);
            usersScroll.setHvalue (hValue - delta / users.getWidth ());
        });
        
        /* init method */ makeDefaultTabs ();
        
        /*
        TextArea input = SceneComponent.INPUT.get (scene);
        input.addEventHandler (KeyEvent.KEY_PRESSED, ke -> {
            if (!SEND_TRIGGER.match (ke)) { return; }
            readAndendIfPossible (input);
        });
        */

        DetachableTabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
        conversations.getSelectionModel ().selectedItemProperty ()
                     .addListener ((tabs, prev, next) -> {
             Node content = next.getContent ();
             if (!(content instanceof AbsTabContent)) { return; }
             //currentConversation.setInput (input.getText ());
             currentConversation = (AbsTabContent) content;
             
             AbsTabContent tabContent = (AbsTabContent) content;
             tabContent.onResponsibleTabOpened (next);
             //input.setText (tabContent.getInput ());
        });
        
        /* init method */ makeDefaultButtons (null);
        
        Timeline clockLineUpdator = new Timeline (
            new KeyFrame (Duration.ZERO, this::updateClockLine),
            new KeyFrame (Duration.seconds (1)));
        clockLineUpdator.setCycleCount (Timeline.INDEFINITE);
        clockLineUpdator.playFromStart ();
        
        manager.getSharedContext ()
        . getTasksService ()
        . subscribe (sceneListener);
        
        manager.getSharedContext ()
        . getUsersService ()
        . subscribe (sceneListener);
    }
    
    private final void makeDefaultTabs () {
        Tab publicChatTab = new Tab ("public");
        openedTabs.put (publicChatTab.getText (), publicChatTab);
        final Conversation publicConversation 
            = new Conversation (this, publicChatTab.getText ());
        knownConversations.put (publicChatTab.getText (), 
                                     publicConversation);
        this.currentConversation = publicConversation;
        publicChatTab.setContent (publicConversation);
        publicChatTab.setClosable (false);

        Tab tasksChatTab = new Tab ("tasks");
        openedTabs.put (tasksChatTab.getText (), tasksChatTab);
        final Conversation tasksConversation
            = new TasksConversation (this, tasksChatTab.getText ());
        knownConversations.put (tasksChatTab.getText (), 
                                     tasksConversation);
        tasksChatTab.setContent (tasksConversation);
        tasksChatTab.setClosable (false);
        
        final String ratingName = "rating";
        Tab ratingMonitorTab = new Tab (ratingName);
        ratingMonitorTab.setContent (new RatingMonitor (this, ratingName));
        openedTabs.put (ratingName, ratingMonitorTab);
        ratingMonitorTab.setClosable (false);

        DetachableTabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
        
        conversations.getTabs ().add (publicChatTab);
        conversations.getTabs ().add (tasksChatTab);
        conversations.getTabs ().add (ratingMonitorTab);
        conversations.getSelectionModel ().clearAndSelect (0);
    }
    
    private final void makeDefaultButtons (TextArea input) {
        Button reconnect = SceneComponent.RECONNECT_BUTTON.get (scene);
        reconnect.setOnAction (__ -> {
            new Thread (manager.getSharedContext ().getClientAdapter ()::performReconnection)
              . start (); // Not to block GUI thread
        });
        
        /*
        Button clearBuffer = SceneComponent.CLEAR_BUFFER.get (scene);
        clearBuffer.setOnMouseClicked (__ -> clearBuffer ());
        
        Button send = SceneComponent.SEND.get (scene);
        send.setGraphic (manager.getSharedContext ().getMessageInterpreter ()
                                .getIcon ("send", 36, 20));
        send.setOnAction (__ -> readAndendIfPossible (input));
        send.setBackground (Background.EMPTY);
        send.setCursor (Cursor.HAND);
        
        Button smile = SceneComponent.SMILE.get (scene);
        smile.setGraphic (manager.getSharedContext ().getMessageInterpreter ()
                                 .getIcon ("smile", 24, 24));
        smile.setBackground (Background.EMPTY);
        smile.setCursor (Cursor.HAND);
        
        Button attach = SceneComponent.ATTACH.get (scene);
        attach.setBackground (Background.EMPTY);
        attach.setCursor (Cursor.HAND);
        */
    }
    
    private void updateClockLine (ActionEvent actionEvent) {
        Trio <Long, Long, ClockStatus> info = manager.getSharedContext ()
                                            . getMessageHistory ()
                                            . getInfoAboutClock ();
        Optional.ofNullable (SceneComponent.CLOCK_TIME.<Label> get (scene))
                .ifPresent (label -> {
            LocalDateTime time = LocalDateTime.ofEpochSecond (info.F, 0, UTC);
            label.setText (time.format (DateTimeFormatter.ISO_LOCAL_TIME));
        });
        Optional.ofNullable (SceneComponent.CLOCK_TOTAL.<Label> get (scene))
                .ifPresent (label -> {
            LocalDateTime time = LocalDateTime.ofEpochSecond (info.S, 0, UTC);
            label.setText (time.format (DateTimeFormatter.ISO_LOCAL_TIME));
        });
        Optional.ofNullable (SceneComponent.CLOCK_STATUS.<Label> get (scene))
                .ifPresent (label -> {
            label.setText (String.format ("(%s)", info.T));
        });
    }
    
    @SuppressWarnings ("unused")
    private void readAndSendIfPossible (TextArea input) {
        if (!currentConversation.isSendingMessageEnable ()) { return; }
        if (input.getText ().trim ().length () == 0) { return; }
        sendMessage (input.getText ());
        input.setText ("");
    }
    
    public void setInInputArea (String value) {
        TextArea input = SceneComponent.INPUT.get (scene);
        Platform.runLater (() -> input.setText (value));
    }
    
    private void sendMessage (String body) {
        if (messageBuffer != null) {
            _sendSpecialMessage (messageBuffer.getID (), body);
            clearBuffer (); return;
        }
        
        ClientAdapter adapter = manager.getSharedContext ().getClientAdapter ();
        final String dialog = currentConversation.getDialog (),
                     id     = StringUtils.randomString (32),
                     author = manager.getSharedContext ().getConfigStorage ()
                            . get ("login").orElse ("[user name]");
        final LocalDateTime time = LocalDateTime.now ();
        final MessageAccess access = currentConversation.getAccess ();
        final String recipient = access.equals (PUBLIC) ? "" : dialog;
        
        MessageEntity message = new MessageEntity (dialog, id, 
                       time, author, recipient, body, access);
        currentConversation.setInput ("");
        adapter.sendMessage (message);
    }
    
    private volatile MessageEntity messageBuffer;
    
    public void placeInBuffer (MessageEntity message) {
        this.messageBuffer = message;
        Platform.runLater (() -> {
            Optional.ofNullable (SceneComponent.BUFFER.<Label> get (scene))
                    .ifPresent (label -> {
                DateTimeFormatter format = MessageCell.DATE_FORMAT;
                final String time   = message.getTime ().format (format),
                             author = message.getAuthor (),
                             body   = message.getBody ();
                label.setText (String.format ("%s (%s) %s", time, author, body));
            });
        });
    }
    
    private void clearBuffer () {
        this.messageBuffer = null;
        Platform.runLater (() -> {
            Optional.ofNullable (SceneComponent.BUFFER.<Label> get (scene))
                    .ifPresent (label -> label.setText (""));
        });
    }
    
    private void _sendSpecialMessage (String id, String body) {
        EditMessageExtension editMessage = new EditMessageExtension (id, EditActionType.EDIT, body);
        manager.getSharedContext ().getCustomExtensionProvider ().send (editMessage, "");
    }
    
    private synchronized void onTabClosed (Event event) {
        Tab source = (Tab) event.getSource ();
        
        synchronized (openedTabs) {
            String key = source.getText ();
            if (source.getContent () instanceof Conversation) {
                Conversation conversation = (Conversation) source.getContent ();
                key = conversation.getDialog ();
            }
            
            openedTabs.remove (key);
        }
    }
    
    synchronized Conversation getOrCreateAndGetConversation (String title) {
        Conversation conversation = knownConversations.get (title);
        if (conversation != null) { return conversation; }
        return createConversation (title);
    }
    
    public synchronized Conversation createConversation (String title) {
        Conversation conversation = knownConversations.get (title);
        if (conversation != null) { return conversation; }
        
        conversation = knownConversations.putIfAbsent (title, 
                             new Conversation (this, title));
        if (conversation == null) { // new conversation created
            conversation = knownConversations.get (title);
        }
        
        return conversation;
    }
    
    public Tab getOrCreateAndGetTabFor (String title, Node content) {
        synchronized (openedTabs) {
            Tab tab = openedTabs.get (title);
            if (tab != null) { return tab; }
            
            final Tab created = new Tab (title, content);
            created.setOnClosed (this::onTabClosed);
            openedTabs.putIfAbsent (title, created);
            
            Platform.runLater (() -> {
                DetachableTabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
                conversations.getTabs ().add (created);
            });
            
            return created;
        }
    }
    
}
