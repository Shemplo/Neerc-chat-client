package ru.shemplo.chat.neerc.network;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.stream.Stream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException.ConnectionException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.network.listeners.BaseConnectionListener;
import ru.shemplo.chat.neerc.network.listeners.BasePacketListener;
import ru.shemplo.chat.neerc.network.listeners.ChatPacketsFilter;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.annot.Wind;

@Slf4j
@Snowflake
@SuppressWarnings ("unused")
public class ConnectionService {
    
    private ConnectionListener connectionListener;
    private StanzaListener stanzaListener;
    private ClientAdapter clientAdapter;
    private ConfigStorage configStorage;
    private StanzaFilter stanzaFilter;
    
    @Snowflake (manual = true)
    @Getter private volatile AbstractXMPPConnection connection;
    
    @Snowflake (manual = true)
    @Setter private MultiUserChat multiUserChat;
    
    private AbstractXMPPConnection prepareConnection () throws XmppStringprepException {
        final String login    = configStorage.get ("login")   .orElse ("test"), 
                     password = configStorage.get ("password").orElse ("test"),
                     host     = configStorage.get ("host")    .orElse ("localhost"),  
                     domain   = configStorage.get ("domain")  .orElse ("localhost");
        final int    port     = configStorage.get ("port", Integer::parseInt).orElse (5222);
        final SecurityMode security  = configStorage.get ("security", SecurityMode::valueOf)
                                     . orElse (SecurityMode.disabled);
        Builder configurationBuilder = XMPPTCPConnectionConfiguration.builder ()
                                     . setResource (StringUtils.randomString (10))
                                     . setUsernameAndPassword (login, password)
                                     . setCompressionEnabled (true)
                                     . setSecurityMode (security)
                                     . setXmppDomain (domain)
                                     . setHost (host)
                                     . setPort (port);
        Optional.ofNullable (createSSLContext ())
                .ifPresent (configurationBuilder::setCustomSSLContext);
        AbstractXMPPConnection connection = Stream.of (configurationBuilder)
                                          . map (Builder::build)
                                          . map (XMPPTCPConnection::new)
                                          . findFirst ().get ();
        connection.addSyncStanzaListener (stanzaListener, stanzaFilter);
        connection.addConnectionListener (connectionListener);
        return connection;
    }
    
    private SSLContext createSSLContext () {
        try (
            InputStream is = ConnectionService.class
                           . getResourceAsStream ("/chat.jks");
        ) {
            final String password = configStorage.get ("jks.password").orElse ("");
            KeyStore store = KeyStore.getInstance ("JKS");
            store.load (is, password.toCharArray ());
            
            KeyManagerFactory factory = KeyManagerFactory
                                      . getInstance (KeyManagerFactory
                                                     . getDefaultAlgorithm ());
            factory.init (store, password.toCharArray ());
            
            SSLContext context = SSLContext.getInstance ("TLS");
            context.init (factory.getKeyManagers (), null, new SecureRandom ());
            
            return context;
        } catch (IOException | GeneralSecurityException es) {
            es.printStackTrace ();
        }
        
        return null;
    }
    
    public EntityBareJid prepareEntityJid () {
        final String domain = configStorage.get ("domain").orElse ("localhost"),
                     room   = configStorage.get ("room").orElse ("neerc");
        return JidCreate.entityBareFromOrThrowUnchecked (
            String.format ("%s@conference.%s", room, domain));
    }
    
    public void connect () throws IOException, InterruptedException, 
            SmackException, XMPPException {
        disconnect (); // Closing (safe) previous connection if it exists
        
        try {            
            this.connection = prepareConnection ();
            getConnection ().connect ();
        } catch (ConnectionException | NoResponseException es) {
            connectionListener.connectionClosedOnError (es);
        }
    }
    
    public void disconnect () {
        if (connection != null && connection.isConnected ()) { 
            connection.disconnect (); 
        }
    }
    
    public void sendMessage (String message) {
        try {
            multiUserChat.sendMessage (message);
        } catch (NotConnectedException 
              | InterruptedException es) {
            es.printStackTrace ();
        }
    }
    
}
