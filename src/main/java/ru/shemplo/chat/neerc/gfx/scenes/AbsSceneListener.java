package ru.shemplo.chat.neerc.gfx.scenes;

import java.util.Objects;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.listeners.ConnectionStatus;

public abstract class AbsSceneListener implements SceneListener {
    
    protected static final Random RANDOM = new Random ();
    protected static final int ANIMATION_FRAMES = 100;
    
    @Getter protected final WindowManager manager;
    @Getter protected final Scene scene;
    
    protected final Timeline 
        fadeIN = new Timeline (
            new KeyFrame (Duration.ZERO, __ -> {
                double delta = 1.0 / ANIMATION_FRAMES;
                Parent root = getScene ().getRoot ();
                double current = root.getOpacity ();
                
                current = Math.min (current, 1.0 - delta);
                root.setOpacity (current + delta);
            }),
            new KeyFrame (Duration.millis (3))
        ),
        fadeOUT = new Timeline (
            new KeyFrame (Duration.ZERO, __ -> {
                double delta = 1.0 / ANIMATION_FRAMES;
                Parent root = getScene ().getRoot ();
                double current = root.getOpacity ();
                
                current = Math.max (current, delta);
                root.setOpacity (current - delta);
            }),
            new KeyFrame (Duration.millis (3))
        );
    
    private Point2D capture = null;
    
    protected AbsSceneListener (WindowManager manager, Scene scene) {
        this.manager = manager; this.scene = scene;
        
        scene.setOnMousePressed (me -> { capture = new Point2D (me.getSceneX (), me.getSceneY ()); });
        scene.setOnMouseDragged (me -> {
            final Stage stage = manager.getStage ();
            if (!Objects.isNull (capture) && MouseButton.PRIMARY.equals (me.getButton ())) {
                stage.setX (me.getScreenX () - capture.getX ());
                stage.setY (me.getScreenY () - capture.getY ());
            }
        });
    }
    
    protected void switchSceneWithAnimation (ClientScene to) {
        fadeOUT.setCycleCount (ANIMATION_FRAMES);
        fadeOUT.playFromStart ();
        
        new Thread (() -> {
            try { Thread.sleep (500); } catch (Exception e) { return; }
            manager.switchScene (to);
        }).start ();;
    }
    
    @Override
    public void onSceneShown () {
        fadeIN.setCycleCount (ANIMATION_FRAMES);
        fadeIN.playFromStart ();
    }
    
    public void onConnectionStatusChanged (ConnectionStatus status, String message) {
        final boolean isConnected = ConnectionStatus.CONNECTED.equals (status);
        final ConfigStorage configStorage = manager.getConfigStorage ();
        final UsersService usersService = manager.getUsersService ();
        
        SceneComponent.LOGIN.<Label> safe (scene).ifPresent (label -> {
            final String login = configStorage.get ("login").orElse ("[user name]");
            Platform.runLater (() -> {
                label.setTextFill (usersService.getColorForName (login));
                label.setText (login);
            });
        });
        
        SceneComponent.CONNECTION_ICON.<ImageView> safe (scene).ifPresent (view -> {
            final String iconName = isConnected ? "online" : "offline";
            final String icon= String.format ("/gfx/%s.png", iconName);
            final Image image = new Image (icon, 32d, 32d, true, true);
            
            Platform.runLater (() -> { view.setImage (image); });
        });
        
        SceneComponent.RECONNECT_BUTTON.<Button> safe (scene).ifPresent (button -> {
            Platform.runLater (() -> button.setDisable (false));
        });
        
        SceneComponent.CONNECTION_STATUS.<Label> safe (scene).ifPresent (label -> {
            Platform.runLater (() -> label.setText (isConnected ? "" : message));
        });
    }
    
}
