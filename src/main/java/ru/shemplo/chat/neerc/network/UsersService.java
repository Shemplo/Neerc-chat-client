package ru.shemplo.chat.neerc.network;

import static java.util.Collections.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.PresenceMessageEntity;
import ru.shemplo.chat.neerc.enities.UserEntity;
import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;
import ru.shemplo.chat.neerc.enities.UserEntity.UserPower;
import ru.shemplo.chat.neerc.network.listeners.UserPresenceListener;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class UsersService {
    
    private MessageService messageService;
    
    private final ConcurrentMap <String, UserEntity> 
        users = new ConcurrentHashMap <> ();
    
    private final Object STUB_OBJECT = new Object ();
    private final ConcurrentMap <UserPresenceListener, Object> 
        listeners = new ConcurrentHashMap <> ();
    
    public void subscribe (final UserPresenceListener listener) {
        listeners.computeIfAbsent (listener, k -> {
            listener.onUsersUpdated ();
            return STUB_OBJECT;
        });
    }
    
    public synchronized void mergeUsers (Collection <UserEntity> users) {
        AtomicBoolean sendNotification = new AtomicBoolean (false);
        users.forEach (user -> {
            this.users.compute (user.getName (), (k, v) -> {
                if (v == null) { 
                    sendNotification.set (true);
                    return user; 
                }
                
                v.setGroup (user.getGroup ());
                v.setPower (user.getPower ());
                return v;
            });
        });
        
        if (sendNotification.get ()) {
            listeners.keySet ().stream ()
            . forEach (UserPresenceListener::onUsersUpdated);            
        }
    }
    
    public synchronized UserEntity getOrCreateAndGetUser (String name) {
        if (!users.containsKey (name)) {
            UserEntity enity = new UserEntity (name, "unknown", UserPower.PARTICIPANT);
            users.putIfAbsent (name, enity);
            
            listeners.keySet ().stream ()
            . forEach (UserPresenceListener::onUsersUpdated);
        }
        
        return users.get (name);
    }
    
    public void changeUserPresence (String user, OnlineStatus status, 
            LocalDateTime time) {
        Objects.requireNonNull (status);
        
        UserEntity userEnity = getOrCreateAndGetUser (user);
        if (userEnity.getStatus ().equals (status)) { return; }
        
        userEnity.setStatus (status);
        HBox line = new HBox (new Label (user), new Label (String.format (" is %s", status)));
        ((Label) line.getChildren ().get (0)).setTextFill (getColorForName (user));
        
        MessageEntity message = new PresenceMessageEntity (time, user, status);
        messageService.addMessage (message);
        
        listeners.keySet ().stream ()
        . forEach (lis -> lis.onUserChangedPresence (user, status));
    }
    
    public boolean isUser (String name) {
        return users.containsKey (name);
    }
    
    public boolean isPower (String name) {
        return isUser (name) 
            && users.get (name).getPower ()
               . equals (UserPower.POWER);
    }
    
    public Collection <UserEntity> getUsers () {
        return unmodifiableCollection (users.values ());
    }
    
    public void clear () {
        users.clear (); // It can be used during reconnection
        listeners.keySet ().stream ()
        . forEach (UserPresenceListener::onUsersUpdated);
    }
    
    private final ConcurrentMap <String, Color> 
        colors = new ConcurrentHashMap <> ();
    
    public Color getColorForName (String name) {
        if (!colors.containsKey (name)) {
            Color color = getRandomColor (32, 160);
            colors.putIfAbsent (name, color);
        }
        
        return colors.get (name);
    }
    
    private static final Random RANDOM = new Random ();
    
    private Color getRandomColor (int from, int to) {
        return Color.rgb (
                from + RANDOM.nextInt (to - from), 
                from + RANDOM.nextInt (to - from), 
                from + RANDOM.nextInt (to - from)
             );
    }
    
    public void generateColorsAgain () {
        colors.keySet ().forEach (name -> {
            colors.computeIfPresent (name, 
                (k, v) -> getRandomColor (32, 160));
        });
    }
    
    private final Random random = new Random ();
    
    private final Color getRandomColor (int from, int to) {
        return javafx.scene.paint.Color.rgb (
                from + random.nextInt (to - from), 
                from + random.nextInt (to - from), 
                from + random.nextInt (to - from)
             );
    }
    
}
