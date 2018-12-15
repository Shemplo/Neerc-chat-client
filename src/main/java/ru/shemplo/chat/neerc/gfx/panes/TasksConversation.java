package ru.shemplo.chat.neerc.gfx.panes;

import com.panemu.tiwulfx.control.DetachableTabPane;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.scenes.SceneComponent;
import ru.shemplo.chat.neerc.gfx.scenes.SceneHolder;

public class TasksConversation extends Conversation {
    
    public TasksConversation (SceneHolder listener, String dialog) {
        super (listener, dialog, false);
    }
    
    @Override
    public boolean onAdded (MessageEntity message) {
        if (!super.onAdded (message)) { return false; }
        final Scene scene = holder.getScene ();

        DetachableTabPane conversations = SceneComponent.CONVERSATIONS.get (scene);
        Tab owner = holder.getOrCreateAndGetTabFor (dialog, this);
        int index = conversations.getTabs ().indexOf (owner);
        if (index != -1) {
            conversations.getSelectionModel ().select (index);
        }
        
        return true;
    }
    
}
