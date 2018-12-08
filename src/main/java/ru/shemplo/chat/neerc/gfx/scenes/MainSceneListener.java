package ru.shemplo.chat.neerc.gfx.scenes;

import static java.time.ZoneOffset.*;
import static ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jivesoftware.smack.util.StringUtils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess;
import ru.shemplo.chat.neerc.enities.UserEntity;
import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;
import ru.shemplo.chat.neerc.enities.UserEntity.UserPower;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.gfx.panes.Conversation;
import ru.shemplo.chat.neerc.gfx.panes.MessageCell;
import ru.shemplo.chat.neerc.gfx.panes.TaskTile;
import ru.shemplo.chat.neerc.gfx.panes.TasksConversation;
import ru.shemplo.chat.neerc.network.TasksService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.exten.ClockExtension.ClockStatus;
import ru.shemplo.chat.neerc.network.exten.editor.EditMessageExtension;
import ru.shemplo.chat.neerc.network.exten.editor.EditMessageExtension.EditActionType;
import ru.shemplo.chat.neerc.network.listeners.ConnectionStatusListener;
import ru.shemplo.chat.neerc.network.listeners.TasksStatusListener;
import ru.shemplo.chat.neerc.network.listeners.UserPresenceListener;
import ru.shemplo.snowball.stuctures.Pair;
import ru.shemplo.snowball.stuctures.Trio;

public class MainSceneListener extends AbsSceneListener 
    implements ConnectionStatusListener, UserPresenceListener, 
        TasksStatusListener {
    
    @Getter private Conversation currentConversation;
    
    private static final KeyCodeCombination 
        SEND_TRIGGER = new KeyCodeCombination (KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN);
    private final Map <String, Conversation> knownConversations = new ConcurrentHashMap <> ();
    private final Map <String, Tab> openedTabs = new ConcurrentHashMap <> ();
    
    protected MainSceneListener (WindowManager manager, Scene scene) {
        super (manager, scene);
        
        Button reconnect = SceneComponent.RECONNECT_BUTTON.get (scene);
        reconnect.setOnAction (__ -> {
            new Thread (manager.getSharedContext ().getClientAdapter ()::performReconnection)
              . start (); // Not to block GUI thread
        });
        
        ScrollPane usersScroll = SceneComponent.USERS_SCROLL.get (scene);
        usersScroll.setBackground (Background.EMPTY);
        usersScroll.setBorder (Border.EMPTY);
        usersScroll.setOnScroll (se -> {
            double hValue = usersScroll.getHvalue (), delta = se.getDeltaY ();
            HBox users = SceneComponent.USERS.get (scene);
            usersScroll.setHvalue (hValue - delta / users.getWidth ());
        });
        
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
        
        TextArea input = SceneComponent.INPUT.get (scene);
        input.addEventHandler (KeyEvent.KEY_PRESSED, ke -> {
            if (!SEND_TRIGGER.match (ke)) { return; }
            
            if (!currentConversation.isSendingMessageEnable ()) { return; }
            if (input.getText ().trim ().length () == 0) { return; } 
            sendMessage (input.getText ());
            input.setText ("");
        });
        
        TabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
        conversations.getTabs ().add (publicChatTab);
        conversations.getTabs ().add (tasksChatTab);
        
        conversations.getSelectionModel ().selectedItemProperty ()
                     .addListener ((tabs, prev, next) -> {
             Node content = next.getContent ();
             if (!(content instanceof Conversation)) { return; }
             currentConversation.setInput (input.getText ());
             this.currentConversation = (Conversation) content;
             
             Conversation conversation = (Conversation) content;
             conversation.onResponsibleTabOpened (next);
             input.setText (conversation.getInput ());
        });
        conversations.getSelectionModel ().clearAndSelect (0);
        
        Button clearBuffer = SceneComponent.CLEAR_BUFFER.get (scene);
        clearBuffer.setOnMouseClicked (__ -> clearBuffer ());
        
        Button send = SceneComponent.SEND.get (scene);
        send.setGraphic (manager.getSharedContext ().getMessageInterpreter ()
                                .getIcon ("send", 36, 20));
        send.setOnAction (__ -> {
            if (!currentConversation.isSendingMessageEnable ()) { return; }
            if (input.getText ().trim ().length () == 0) { return; }
            sendMessage (input.getText ());
            input.setText ("");
        });
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
        
        Timeline clockLineUpdator = new Timeline (
            new KeyFrame (Duration.ZERO, this::updateClockLine),
            new KeyFrame (Duration.seconds (1)));
        clockLineUpdator.setCycleCount (Timeline.INDEFINITE);
        clockLineUpdator.playFromStart ();
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

    @Override
    public void onUserChangedPresence (String user, OnlineStatus status) {
        Image image = Stream.of (status)
                    . map (OnlineStatus::name)
                    . map (String::toLowerCase)
                    . map (s -> String.format ("/gfx/user-%s.png", s))
                    . map (Image::new)
                    . findFirst ()
                    . get ();
        final ImageView view = new ImageView (image);
        SceneComponent.USERS.<HBox> safe (scene).ifPresent (line -> {
            Platform.runLater (() -> {
                line.getChildren ().stream ()
                    .map       (o -> (Button) o)
                    .filter    (b -> b.getText ().equals (user))
                    .findFirst ()
                    .ifPresent (b -> b.setGraphic (view));
            });
        });
    }

    private static final Comparator <UserEntity> 
        UE_COMPARATOR = (a, b) -> {
            if (Objects.equals (a.getPower (), b.getPower ())) {
                if (Objects.equals (a.getGroup (), b.getGroup ())) {
                    return a.getName ().compareTo (b.getName ());
                }
                
                return a.getGroup ().compareTo (b.getGroup ());
            }
            
            return UserPower.POWER.equals (a.getPower ()) ? -1 : 1;
        };
    
    @Override
    public void onUsersUpdated () {
        SceneComponent.USERS.<HBox> safe (scene).ifPresent (line -> {
            Platform.runLater (line.getChildren ()::clear);
            
            final UsersService usersService = manager.getUsersService ();
            List <Button> buttons = usersService.getUsers ().stream ()
            . sorted (UE_COMPARATOR)
            . map (u -> Pair.mp (u, u.getPower ().name ().toLowerCase ()))
            . map (p -> p.applyS (n -> String.format ("status-%s", n)))
            . map (p -> {
                Image status = Stream.of (p.F.getStatus ())
                             . map (OnlineStatus::name)
                             . map (String::toLowerCase)
                             . map (s -> String.format ("/gfx/user-%s.png", s))
                             . map (Image::new).findFirst ().get ();
                final ImageView view = new ImageView (status);
                Button button = new Button (p.F.getName (), view);
                button.setOnMouseClicked (this::onUserButtonClick);
                button.getStyleClass ().add ("metal-button");
                button.getStyleClass ().add (p.S);
                button.setGraphicTextGap (8.0);
                return button;
            })
            . collect (Collectors.toList ());
            Platform.runLater (() -> line.getChildren ().addAll (buttons));
        });
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
    
    private void onUserButtonClick (MouseEvent me) {
        Button source = (Button) me.getSource ();
        final String title = source.getText ();
        //System.out.println (String.format ("title %s", title));
        Conversation conversation = getOrCreateAndGetConversation (title);
        Tab tab = getOrCreateAndGetTabFor (title, conversation);
        
        TabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
        int index = conversations.getTabs ().indexOf (tab);
        if (index != -1) {
            conversations.getSelectionModel ().select (index);
        }
    }
    
    private synchronized Conversation getOrCreateAndGetConversation (String title) {
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
                TabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
                conversations.getTabs ().add (created);
            });
            
            return created;
        }
    }
    
    private final ConcurrentMap <String, TaskTile> 
        tasks = new ConcurrentHashMap <> (); 

    @Override
    public synchronized void onTasksUpdated () {
        SceneComponent.TASKS.<VBox> safe (scene).ifPresent (line -> {
            Platform.runLater (line.getChildren ()::clear);
            
            final TasksService tasksService = manager.getTasksService ();
            final String user = manager.getConfigStorage ().get ("login")
                              . orElse ("[user name]");
            List <TaskTile> tiles = tasksService.getActualTasksFor (user).stream ()
                                  . sorted  ((a, b) -> a.getTime ().compareTo (b.getTime ()))
                                  . map     (task -> Pair.mp(task, new TaskTile (this, task)))
                                  . peek    (p -> tasks.put (p.F.getId (), p.S))
                                  . map     (p -> p.S)
                                  . collect (Collectors.toList ());
            Platform.runLater (() -> line.getChildren ().addAll (tiles));
        });
    }

    @Override
    public void onTaskUpdated (String id) {
        manager.getTasksService ().getTaskByID (id)
               .ifPresent (task -> {
            TaskTile tile = tasks.get (task.getId ());
            if (tile == null) { return; }
            
            final String user = manager.getConfigStorage ().get ("login")
                              . orElse ("[user name]");
            task.getStatusFor (user).ifPresent (status -> {                
                tile.changeStatusTo (status.F, status.S);
            });
        });
    }
    
}
