package ru.shemplo.chat.neerc.network;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.network.listeners.MessageHistoryListener;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class MessageService {
    
    @Cooler public static MessageService shapeMessageHistory () {
        return new MessageService ();
    }
    
    @Init private ClientAdapter clientAdapter;
    
    private final Object STUB_OBJECT = new Object ();
    private final ConcurrentMap <MessageHistoryListener, Object> 
        listeners = new ConcurrentHashMap <> ();
    private final ConcurrentMap <String , LocalDateTime>
        updates = new ConcurrentHashMap <> ();
    private final Queue <MessageEntity> 
        messages = new ConcurrentLinkedQueue <> ();
    
    public void subscribe (final MessageHistoryListener listener) {
        listeners.computeIfAbsent (listener, k -> {
            listener.getListeningDialogsName ().forEach (name -> {
                listener.onDialogUpdated (name);
            });
            
            return STUB_OBJECT;
        });
    }
    
    public void addMessage (MessageEntity message) {
        messages.add (message);
        
        final String dialog = message.getDialog ();
        updates.put (dialog, LocalDateTime.now ());
        
        long listenrs = listeners.keySet ().stream ()
                      . filter (lis -> lis.getListeningDialogsName ().contains (dialog))
                      . peek   (lis -> lis.onDialogUpdated (dialog))
                      . count  ();
        if (listenrs == 0) { clientAdapter.onMessageInUnsubscribedDialog (dialog); }
    }
    
    public Collection <MessageEntity> getMessagesInDialog (String dialog) {
        return messages.stream ()
             . filter  (message -> message.getDialog ().equals (dialog))
             . collect (Collectors.toList ());
    }
    
    public void deleteMessage (String dialog, String id) {
        if (messages.removeIf (message -> message.getID ().equals (id))) {
            updates.put (dialog, LocalDateTime.now ()); // just udated
            long listenrs = listeners.keySet ().stream ()
                          . filter (lis -> lis.getListeningDialogsName ().contains (dialog))
                          . peek   (lis -> lis.onDialogUpdated (dialog))
                          . count  ();
            if (listenrs == 0) { clientAdapter.onMessageInUnsubscribedDialog (dialog); }
        }
    }
    
}
