package ru.shemplo.chat.neerc.gfx.scenes;

import javafx.scene.Scene;
import ru.shemplo.chat.neerc.gfx.WindowManager;

public interface SceneListener {
    
    public WindowManager getManager ();
    
    public void onSceneShown ();
    
    public Scene getScene ();
    
    
}
