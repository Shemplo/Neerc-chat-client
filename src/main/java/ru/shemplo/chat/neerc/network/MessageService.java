package ru.shemplo.chat.neerc.network;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.network.exten.ClockExtension.ClockStatus;
import ru.shemplo.chat.neerc.network.listeners.MessageListener;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.stuctures.Trio;

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
    
    public void editMessage (String id, String body) {
        synchronized (messages) {
            messages.stream ()
            . filter  (m -> id.equals (m.getID ()))
            . forEach (m -> m.setBody (body));
        }
        
        notifyAboutChanges (lis -> lis.onEdited (id));
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
    
    private volatile LocalDateTime clockUpdated = LocalDateTime.now ();
    private volatile ClockStatus status = ClockStatus.OVER;
    private volatile long time = 0, total = 0;
    
    public void synchronizeClock (long time, long total, ClockStatus clockStatus) {
        this.time = time / 1000; this.total = total / 1000;
        this.status = status;
        
        clockUpdated = LocalDateTime.now ();
    }
    
    public Trio <Long, Long, ClockStatus> getInfoAboutClock () {
        long delta = LocalDateTime.now ().toEpochSecond (ZoneOffset.UTC) 
                   - clockUpdated.toEpochSecond (ZoneOffset.UTC);
        if (!ClockStatus.RUNNING.equals (status)) { delta = 0; }
        return Trio.mt (time + delta, total, status);
    }
    
}
