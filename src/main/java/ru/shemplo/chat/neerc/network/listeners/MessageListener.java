package ru.shemplo.chat.neerc.network.listeners;

import ru.shemplo.chat.neerc.enities.MessageEntity;

public interface MessageListener {
    
    boolean onAdded (MessageEntity message);
    
    boolean onDeleted (String id);
    
}
