package ru.shemplo.chat.neerc.gfx.panes;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import ru.shemplo.chat.neerc.gfx.scenes.SceneComponent;
import ru.shemplo.chat.neerc.gfx.scenes.SceneListener;

public class TasksConversation extends Conversation {

    public TasksConversation (SceneListener listener, String dialog) {
        super (listener, dialog);
        
        this.sendingMessageEnable = false;
    }
    
    @Override
    public void onDialogUpdated (String dialog) {
        super.onDialogUpdated (dialog);
        
        final Scene scene = listener.getScene ();
        TabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
        final Tab owner = listener.getOrCreateAndGetTabFor (dialog, this);
        int index = conversations.getTabs ().indexOf (owner);
        if (index != -1) {
            conversations.getSelectionModel ().select (index);
        }
    }
    
}
