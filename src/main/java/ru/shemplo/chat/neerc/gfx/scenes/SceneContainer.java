package ru.shemplo.chat.neerc.gfx.scenes;

import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public interface SceneContainer {
    
    public String getId ();
    
    default <R extends Node> R get (Parent parent) {
        String key = String.format ("#%s", getId ());
        @SuppressWarnings ("unchecked") R out 
            = (R) parent.lookup (key);
        return out;
    }
    
    default <R extends Node> Optional <R> safe (Parent parent) {
        return Optional.ofNullable (get (parent));
    }
    
    default <R extends Node> R get (Scene scene) {
        String key = String.format ("#%s", getId ());
        @SuppressWarnings ("unchecked") R out 
            = (R) scene.lookup (key);
        return out;
    }
    
    default <R extends Node> Optional <R> safe (Scene scene) {
        return Optional.ofNullable (get (scene));
    }
    
}
