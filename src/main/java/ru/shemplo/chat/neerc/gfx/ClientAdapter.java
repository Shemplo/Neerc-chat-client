package ru.shemplo.chat.neerc.gfx;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.config.SharedContext;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess;
import ru.shemplo.chat.neerc.network.ConnectionService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;

@Slf4j
@Snowflake
public class ClientAdapter {
 
    @Cooler public static ClientAdapter shapeClientAdapter () {
        return new ClientAdapter ();
    }
    
    @Getter @Init private SharedContext sharedContext;
    @Init private ConnectionService connectionService;
    @Init private ConfigStorage configStorage;
    @Init private UsersService usersService;
    
    private WindowManager windowManager;
    
    public void clientWindowInitialized (WindowManager manager) {
        manager.setSharedContext (sharedContext);
        this.windowManager = manager;
    }
    
    public void onMessageInUnsubscribedDialog (String dialog) {
        windowManager.createConversation (dialog);
    }

    public void performReconnection () {        
        try {
            Thread.sleep (100); // Waiting for GUI updates
            connectionService.connect ();
        } catch (IOException | InterruptedException 
              | SmackException | XMPPException e) {
            // TODO: handler of exceptions
            log.error (e.getMessage ());
        }
    }
    
    public void performCloseConnection () {
        connectionService.disconnect ();
    }
    
    public void sendMessage (MessageEntity message) {
        StringBuilder sb = new StringBuilder ();
        MessageAccess access = message.getAccess ();
        if (access.equals (MessageAccess.ROOM_PRIVATE)) {
            sb.append (String.format ("%%%s>", message.getRecipient ()));
        } else if (access.equals (MessageAccess.PRIVATE)) {
            sb.append (String.format ("%s>", message.getRecipient ()));
        }
        sb.append (message.getBody ());
        
        connectionService.sendMessage (sb.toString ());
    }
    
}
