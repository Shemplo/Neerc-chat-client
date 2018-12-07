package ru.shemplo.chat.neerc.network.iq;

import javax.activation.UnsupportedDataTypeException;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.network.ConnectionService;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.annot.processor.Snowball;

@Snowflake
public class CustomIQProvider extends IQProvider <AbsCustomIQ> {
    
    @Cooler public static CustomIQProvider shapeCustomIQProvider () {
        CustomIQProvider provider = new CustomIQProvider ();
        Snowball.runOnInited (provider::_initProvider);
        return provider;
    }
    
    @Init private ConnectionService connectionService;
    @Init private ConfigStorage configStorage;
    
    private void _initProvider () {
        final String namespace = prepareNamespace ();
        ProviderManager.addIQProvider ("query", namespace.concat ("#users"), this);
        ProviderManager.addIQProvider ("query", namespace.concat ("#tasks"), this);
    }
    
    public static String prepareNamespace () {
        return "http://neerc.ifmo.ru/protocol/neerc";
    }
    
    public Jid prepareNamespaceJid () {
        final String domain = configStorage.get ("domain").orElse ("localhost"),
                     room   = configStorage.get ("room").orElse ("neerc");
        return JidCreate.fromOrThrowUnchecked (
            String.format ("%s@neerc.%s", room, domain));
    }
    
    public void query (String query) {
        send (new RequestIQ (query));
    }
    
    public void send (RequestIQ requestIQ) {
        requestIQ.setTo (prepareNamespaceJid ());
        
        try { 
            connectionService.getConnection ().sendStanza (requestIQ); 
        } catch (NotConnectedException | InterruptedException es) {
            new RuntimeException (es);
        }
    }

    @Override
    public AbsCustomIQ parse (XmlPullParser parser, int initialDepth) throws Exception {
        AbsCustomIQ iq = null;
        switch (parser.getNamespace ().split ("#") [1]) {
            case "users": iq = new UsersListIQ (); break;
            case "tasks": iq = new TasksListIQ (); break;
            default: 
                throw new UnsupportedDataTypeException ();
        }
        
        iq.read (parser);
        return iq;
    }
    
}
