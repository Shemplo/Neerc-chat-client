package ru.shemplo.chat.neerc.gfx.panes;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.scenes.SceneComponent;
import ru.shemplo.chat.neerc.gfx.scenes.SceneHolder;

public class TasksConversation extends Conversation {

    public TasksConversation (SceneHolder listener, String dialog) {
        super (listener, dialog);
        
        this.sendingMessageEnable = false;
    }
    
    @Override
    public boolean onAdded (MessageEntity message) {
        if (!super.onAdded (message)) { return false; }
        final Scene scene = listener.getScene ();
        
        TabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
        Tab owner = listener.getOrCreateAndGetTabFor (dialog, this);
        int index = conversations.getTabs ().indexOf (owner);
        if (index != -1) {
            conversations.getSelectionModel ().select (index);
        }
        
        return true;
    }
    
}
