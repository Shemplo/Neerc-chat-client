package ru.shemplo.chat.neerc.gfx;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Objects;

import org.jivesoftware.smack.ConnectionListener;

import com.sun.javafx.util.Logging;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.config.SharedContext;
import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;
import ru.shemplo.chat.neerc.gfx.scenes.ClientScene;
import ru.shemplo.chat.neerc.gfx.scenes.MainSceneListener;
import ru.shemplo.chat.neerc.gfx.scenes.SceneListener;
import ru.shemplo.chat.neerc.network.TasksService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.listeners.*;
import sun.util.logging.PlatformLogger.Level;

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
    
    @Getter private volatile SceneListener sceneListener;
    @Getter private ConnectionListener connectionListener;
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
        stage.setMinWidth (725); stage.setMinHeight (675 - 200);
        
        stage.setScene (new Scene (new Pane ()));
        stage.setTitle ("Neerc chat");
        
        switchScene (ClientScene.MAIN);
        //stage.setY ((screen.height - stage.getHeight ()) / 2); 
        //stage.setX ((screen.width - stage.getWidth ()) / 2);
        
        stage.sizeToScene ();
        stage.show ();
        
        stage.setOnCloseRequest (__ -> sharedContext.getClientAdapter ().performCloseConnection ());
        Logging.getCSSLogger ().setLevel (Level.OFF);
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
                
                boolean listenerWasNull = sceneListener == null;
                this.sceneListener = scene.getListener ();
                if (listenerWasNull) {
                    synchronized (this) { this.notify (); }
                }
            }
            
            scene.getListener ().onSceneShown ();
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
        if (sceneHolder.getListener () == null) { return; }
        
        final SceneListener listener = sceneHolder.getListener ();
        if (connectionListener instanceof BaseConnectionListener
                && listener instanceof ConnectionStatusListener) {
            BaseConnectionListener con = (BaseConnectionListener) connectionListener;
            final ConnectionStatus status = con.getCurrentState ();
            final String message = con.getCurrentMessage ();
            
            ConnectionStatusListener sta = (ConnectionStatusListener) listener;
            sta.onConnectionStatusChanged (status, message);
        }
    }

    //@Override
    @Override
    public void onUserChangedPresence (String user, OnlineStatus status) {
        Arrays.asList (ClientScene.values ())
        . forEach (h -> notifySceneListenerAboutUserPresence (h, user, status));
    }
    
    private void notifySceneListenerAboutUserPresence (ClientScene sceneHolder,
            String user, OnlineStatus onlineStatus) {
        if (sceneHolder.getListener () == null) { return; }
        
        final SceneListener listener = sceneHolder.getListener ();
        if (listener instanceof UserPresenceListener) {
            UserPresenceListener pre = (UserPresenceListener) listener;
            pre.onUserChangedPresence (user, onlineStatus);
        }
    }

    @Override
    public void onUsersUpdated () {
        Arrays.asList (ClientScene.values ())
        . forEach (this::notifySceneListenerAboutUsersListUpdated);
    }
    
    private void notifySceneListenerAboutUsersListUpdated (ClientScene sceneHolder) {
        if (sceneHolder.getListener () == null) { return; }
        
        final SceneListener listener = sceneHolder.getListener ();
        if (listener instanceof UserPresenceListener) {
            UserPresenceListener pre = (UserPresenceListener) listener;
            pre.onUsersUpdated ();
        }
    }
    
    public synchronized void createConversation (String title) {
        SceneListener listener = ClientScene.MAIN.getListener ();
        if (listener == null) { return; }
        
        ((MainSceneListener) listener).createConversation (title);
    }

    @Override
    public void onTasksUpdated () {
        Arrays.asList (ClientScene.values ())
        . forEach (this::notifySceneListenerAboutTasksListUpdated);
    }
    
    private void notifySceneListenerAboutTasksListUpdated (ClientScene sceneHolder) {
        if (sceneHolder.getListener () == null) { return; }
        
        final SceneListener listener = sceneHolder.getListener ();
        if (listener instanceof TasksStatusListener) {
            TasksStatusListener tasksListener = (TasksStatusListener) listener;
            tasksListener.onTasksUpdated ();
        }
    }

    @Override
    public void onTaskUpdated (String id) {
        Arrays.asList (ClientScene.values ())
        . forEach (h -> notifySceneListenerAboutTaskUpdated (h, id));
    }
    
    private void notifySceneListenerAboutTaskUpdated (ClientScene sceneHolder, String taskID) {
        if (sceneHolder.getListener () == null) { return; }
        
        final SceneListener listener = sceneHolder.getListener ();
        if (listener instanceof TasksStatusListener) {
            TasksStatusListener tasksListener = (TasksStatusListener) listener;
            tasksListener.onTaskUpdated (taskID);
        }
    }
    
}
