package ru.shemplo.chat.neerc.network.listeners;

import static ru.shemplo.chat.neerc.network.listeners.ConnectionStatus.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException.NotAMucServiceException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.network.ConnectionService;
import ru.shemplo.chat.neerc.network.iq.CustomIQProvider;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;

@Slf4j
@Snowflake
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public final class BaseConnectionListener extends AbstractConnectionListener {

    @Cooler public static BaseConnectionListener shapeChatConnectionListener () {
        return new BaseConnectionListener ();
    }
    
    @Init private ConnectionService connectionService;
    @Init private CustomIQProvider customIQProvider;
    @Init private ClientAdapter clientAdapter;
    @Init private ConfigStorage configStorage;
    
    @Getter private volatile ConnectionStatus 
        currentState = ConnectionStatus.DISCONNECTED;
    @Getter private volatile String currentMessage = "";
    private final Object STUB_OBJECT = new Object ();
    
    private final ConcurrentMap <ConnectionStatusListener, Object> 
        listeners = new ConcurrentHashMap <> ();
    
    public void subscribe (final ConnectionStatusListener listener) {
        listeners.computeIfAbsent (listener, k -> {
            listener.onConnectionStatusChanged (currentState, currentMessage);
            return STUB_OBJECT;
        });
    }
    
    private synchronized void changeStatusAndNotify (ConnectionStatus status, String message) {
        this.currentMessage = message;
        this.currentState = status;
        listeners.keySet ().stream ()
        . forEach (lis -> lis.onConnectionStatusChanged (currentState, message));
    }
    
    @Override
    public void connectionClosed () {
        changeStatusAndNotify (DISCONNECTED, "Connection closed");
    }
    
    @Override
    public void connectionClosedOnError (Exception e) {
        String reason = String.format ("Reason: %s", e.getMessage ());
        changeStatusAndNotify (DISCONNECTED, reason);
    }
    
    @Override
    public void connected (XMPPConnection connection) {
        changeStatusAndNotify (CONNECTING, "Connecting to server...");
        
        if (connection instanceof AbstractXMPPConnection) {
            try {
                ((AbstractXMPPConnection) connection).login ();
            } catch (XMPPException | SmackException 
                  | IOException | InterruptedException es) {
                log.error ("Failed to login on server", es);
                throw new IllegalStateException (es);
            }
        }
        
        changeStatusAndNotify (CONNECTED, "Connected and authorized");
    }
    
    @Override
    public void authenticated (XMPPConnection connection, boolean resumed) {
        changeStatusAndNotify (CONNECTING, "Authentificating...");
        
        EntityBareJid jid = connectionService.prepareEntityJid ();
        MultiUserChat chat = MultiUserChatManager
                           . getInstanceFor (connection)
                           . getMultiUserChat (jid);
        
        final String login = configStorage.get ("login").orElse ("test");
        try {
            final Resourcepart resourcepart = Resourcepart.from (login);
            chat.join (chat.getEnterConfigurationBuilder (resourcepart)
                           .requestMaxStanzasHistory (100)
                           .withPassword ("")
                           .build ());
            Arrays.asList ("users", "tasks")
                  .forEach (customIQProvider::query);
        } catch (XmppStringprepException | NotAMucServiceException 
              | XMPPErrorException | NoResponseException 
              | NotConnectedException | InterruptedException xse) {
            log.error ("Failed to join chat room", xse);
            throw new IllegalStateException (xse);
        }
        
        connectionService.setMultiUserChat (chat);
    }
    
}
