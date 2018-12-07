package ru.shemplo.chat.neerc.gfx.scenes;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiFunction;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.shemplo.chat.neerc.gfx.WindowManager;

@Getter
@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public enum ClientScene {
 
    MAIN ("main", false, MainSceneListener::new);
    
    private final String  filePrefix;
    private final boolean needReload;
    
    private final BiFunction <WindowManager, Scene, SceneListener> 
        producer; // Instance that will be responsible for events
    
    @Getter private SceneListener listener;
    @Getter private boolean inited;
    private Parent root;
    
    public Parent getRoot () {
        if (this.root == null) { reloadRoot (); }
        return this.root;
    }
    
    public Parent reloadRoot () {
        String resourcePath = String.format ("/fxml/%s.fxml", getFilePrefix ());
        URL url = ClientScene.class.getResource (resourcePath);
        
        String stylesPath = String.format ("/css/%s.css", getFilePrefix ());
        try {
            root = FXMLLoader.load (url);
            root.getStylesheets ()
                .add (stylesPath);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return root;
    }
    
    public SceneListener reloadListener (WindowManager manager, Scene scene) {
        inited = true; listener = getProducer ().apply (manager, scene);
        return listener;
    }
    
}
