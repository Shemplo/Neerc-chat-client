package ru.shemplo.chat.neerc.network;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.network.listeners.MessageListener;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class MessageService {
    
    @Cooler public static MessageService shapeMessageHistory () {
        return new MessageService ();
    }
    
    @Init private ClientAdapter clientAdapter;
    
    private final Set <MessageListener> listeners 
          = ConcurrentHashMap.newKeySet ();
    private final Queue <MessageEntity> messages 
          = new ConcurrentLinkedQueue <> ();
    
    public void subscribe (MessageListener listener) {
        listeners.add (listener);
        
        messages.forEach (listener::onAdded);
    }
    
    private long notifyAboutChanges (Predicate <MessageListener> consumer) {
        return listeners.stream ().filter  (consumer).count ();
    }
    
    public void addMessage (MessageEntity message) {
        messages.add (message);
        
        final String dialog = message.getDialog ();
        if (notifyAboutChanges (lis -> lis.onAdded (message)) == 0) {
            clientAdapter.onMessageInUnsubscribedDialog (dialog);
        }
    }
    
    public void deleteMessage (String dialog, String id) {
        if (messages.removeIf (message -> message.getID ().equals (id))) {
            notifyAboutChanges (lis -> lis.onDeleted (id));
        }
    }
    
    public Collection <MessageEntity> getMessagesInDialog (String dialog) {
        return messages.stream ()
             . filter  (message -> message.getDialog ().equals (dialog))
             . collect (Collectors.toList ());
    }
    
    public void markAsReadUntil (String dialog, LocalDateTime time) {
        messages.stream ()
        . filter  (e -> e.getDialog ().equals (dialog))
        . filter  (e -> time.isAfter (e.getTime ()))
        . forEach (e -> e.setRead (true));
    }
    
    
}
