package ru.shemplo.chat.neerc.gfx;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.jivesoftware.smack.ConnectionListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.config.SharedContext;
import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;
import ru.shemplo.chat.neerc.gfx.scenes.ClientScene;
import ru.shemplo.chat.neerc.gfx.scenes.MainSceneHolder;
import ru.shemplo.chat.neerc.gfx.scenes.SceneHolder;
import ru.shemplo.chat.neerc.network.TasksService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.listeners.*;

@Slf4j
public class WindowManager extends Application 
    implements ConnectionStatusListener, UserPresenceListener, 
        TasksStatusListener {

    private static volatile WindowManager instance;
    
    public static WindowManager getInstance () throws InterruptedException {
        if (instance == null) {
            synchronized (WindowManager.class) {
                while (instance == null) {
                    WindowManager.class.wait ();
                }
            }
        }
        
        return instance;
    }
    
    public static Optional <Parent> loadComponent (String name) {
        final String path = String.format ("/fxml/comps/%s.fxml", name);
        URL url = WindowManager.class.getResource (path);
        try {
            return Optional.ofNullable (FXMLLoader.load (url));
        } catch (IOException | NullPointerException se) {
            log.error ("Excpeption: {}", se.getMessage ());
        }
        
        return Optional.empty ();
    }
    
    @Getter private ConnectionListener connectionListener;
    @Getter private volatile SceneHolder sceneHolder;
    @Getter private ConfigStorage configStorage;
    @Getter private SharedContext sharedContext; 
    @Getter private TasksService tasksService;
    @Getter private UsersService usersService;
    @Getter private Stage stage;
    
    public void setSharedContext (SharedContext sharedContext) {
        this.sharedContext = sharedContext;
        
        this.connectionListener = sharedContext.getConnectionListener ();
        this.configStorage = sharedContext.getConfigStorage ();
        this.tasksService = sharedContext.getTasksService ();
        this.usersService = sharedContext.getUsersService ();
        
        ConnectionListener listener = sharedContext.getConnectionListener ();
        if (listener instanceof BaseConnectionListener) {
            ((BaseConnectionListener) listener).subscribe (this);
        }
        
        tasksService.subscribe (this);
        usersService.subscribe (this);
    }
    
    @Override
    public void start (Stage stage) throws Exception {
        synchronized (WindowManager.class) {
            WindowManager.instance = this;
            WindowManager.class.notify ();
            
            this.stage = stage;
        }
        
        @SuppressWarnings ("unused")
        Dimension screen = Toolkit.getDefaultToolkit ().getScreenSize ();
        stage.getIcons ().add (new Image ("/gfx/chat.png"));
        stage.setMinWidth (725); stage.setMinHeight (675);
        
        stage.setScene (new Scene (new Pane ()));
        stage.setTitle ("Neerc chat");
        
        switchScene (ClientScene.MAIN);
        //stage.setY ((screen.height - stage.getHeight ()) / 2); 
        //stage.setX ((screen.width - stage.getWidth ()) / 2);
        
        stage.sizeToScene ();
        stage.show ();
        
        stage.setOnCloseRequest (__ -> sharedContext.getClientAdapter ().performCloseConnection ());
        //Logging.getCSSLogger ().setLevel (Level.OFF);
    }
    
    @Getter private volatile boolean initialized = false;
    
    private void onSceneSwitched () {
        initialized = true;
    }
    
    public void switchScene (ClientScene scene) {
        Objects.requireNonNull (scene);
        
        Parent parent = scene.isNeedReload ()
                      ? scene.reloadRoot ()
                      : scene.getRoot ();
        Platform.runLater (() -> {
            stage.getScene ().setRoot (parent);
            stage.sizeToScene ();
            
            if (scene.isNeedReload () || !scene.isInited ()) {
                scene.reloadListener (this, stage.getScene ());
                
                boolean listenerWasNull = sceneHolder == null;
                this.sceneHolder = scene.getHolder ();
                if (listenerWasNull) {
                    synchronized (this) { this.notify (); }
                }
            }
            
            scene.getHolder ().onSceneShown ();
            stage.sizeToScene ();
            onSceneSwitched ();
        });
    }

    @Override
    public void onConnectionStatusChanged (ConnectionStatus status, String message) {
        Arrays.asList (ClientScene.values ())
        . forEach (this::notifySceneListenerAboutConnectionState);
    }
    
    private void notifySceneListenerAboutConnectionState (ClientScene sceneHolder) {
        if (sceneHolder.getHolder () == null) { return; }
        
        final SceneHolder holder = sceneHolder.getHolder ();
        if (connectionListener instanceof BaseConnectionListener
                && holder instanceof ConnectionStatusListener) {
            BaseConnectionListener con = (BaseConnectionListener) connectionListener;
            final ConnectionStatus status = con.getCurrentState ();
            final String message = con.getCurrentMessage ();
            
            ConnectionStatusListener sta = (ConnectionStatusListener) holder;
            sta.onConnectionStatusChanged (status, message);
        }
    }

    @Override
    public void onUserChangedPresence (String user, OnlineStatus status) {
        Arrays.asList (ClientScene.values ())
        . forEach (h -> notifySceneListenerAboutUserPresence (h, user, status));
    }
    
    private void notifySceneListenerAboutUserPresence (ClientScene sceneHolder,
            String user, OnlineStatus onlineStatus) {
        if (sceneHolder.getHolder () == null) { return; }
        
        final SceneHolder holder = sceneHolder.getHolder ();
        if (holder instanceof UserPresenceListener) {
            UserPresenceListener pre = (UserPresenceListener) holder;
            pre.onUserChangedPresence (user, onlineStatus);
        }
    }

    @Override
    public void onUsersUpdated () {
        Arrays.asList (ClientScene.values ())
        . forEach (this::notifySceneListenerAboutUsersListUpdated);
    }
    
    private void notifySceneListenerAboutUsersListUpdated (ClientScene sceneHolder) {
        if (sceneHolder.getHolder ()
                == null) { return; }
        
        final SceneHolder holder = sceneHolder.getHolder ();
        if (holder instanceof UserPresenceListener) {
            UserPresenceListener pre = (UserPresenceListener) holder;
            pre.onUsersUpdated ();
        }
    }
    
    public synchronized void createConversation (String title) {
        SceneHolder listener = ClientScene.MAIN.getHolder ();
        if (listener == null) { return; }
        
        ((MainSceneHolder) listener).createConversation (title);
    }

    @Override
    public void onTasksUpdated () {
        Arrays.asList (ClientScene.values ())
        . forEach (this::notifySceneListenerAboutTasksListUpdated);
    }
    
    private void notifySceneListenerAboutTasksListUpdated (ClientScene sceneHolder) {
        if (sceneHolder.getHolder () == null) { return; }
        
        final SceneHolder holder = sceneHolder.getHolder ();
        if (holder instanceof TasksStatusListener) {
            TasksStatusListener tasksListener = (TasksStatusListener) holder;
            tasksListener.onTasksUpdated ();
        }
    }

    @Override
    public void onTaskUpdated (String id) {
        Arrays.asList (ClientScene.values ())
        . forEach (h -> notifySceneListenerAboutTaskUpdated (h, id));
    }
    
    private void notifySceneListenerAboutTaskUpdated (ClientScene sceneHolder, String taskID) {
        if (sceneHolder.getHolder () == null) { return; }
        
        final SceneHolder holder = sceneHolder.getHolder ();
        if (holder instanceof TasksStatusListener) {
            TasksStatusListener tasksListener = (TasksStatusListener) holder;
            tasksListener.onTaskUpdated (taskID);
        }
    }
    
}
