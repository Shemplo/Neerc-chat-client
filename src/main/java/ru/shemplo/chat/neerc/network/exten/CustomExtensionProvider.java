package ru.shemplo.chat.neerc.network.exten;

import javax.activation.UnsupportedDataTypeException;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.xmlpull.v1.XmlPullParser;

import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.network.ConnectionService;
import ru.shemplo.chat.neerc.network.exten.editor.EditMessageExtension;
import ru.shemplo.snowball.annot.PostShaped;
import ru.shemplo.snowball.annot.Snowflake;

public class CustomExtensionProvider extends ExtensionElementProvider <AbsCustomExtensionElement> {

    @Snowflake
    public static CustomExtensionProvider shapeClockExtensionProvider () {
        CustomExtensionProvider provider = new CustomExtensionProvider ();
        //Snowball.runOnInited (provider::_initProvider);
        return provider;
    }
    
    private ConnectionService connectionService;
    private ConfigStorage configStorage;
    
    static final EditMessageExtension edit = new EditMessageExtension ();
    static final ClockExtension clock = new ClockExtension ();
    
    @PostShaped
    private void _initProvider () {
        // x / http://neerc.ifmo.ru/protocol/neerc#clock
        ProviderManager.addExtensionProvider (clock.getElementName (), 
                                         clock.getNamespace (), this);
        // edition / http://shemplo.ru/edimes#edit
        ProviderManager.addExtensionProvider (edit.getElementName (), 
                                         edit.getNamespace (), this);
    }
    
    public Jid prepareNamespaceJid (String extension) {
        final String domain = configStorage.get ("domain").orElse ("localhost"),
                     room   = configStorage.get ("room").orElse ("neerc");
        return JidCreate.fromOrThrowUnchecked (
            String.format ("%s@%s.%s", room, extension, domain));
    }
    
    public void send (AbsCustomExtensionElement extensionElement, String body) {
        final String extension = extensionElement.getExtension ();
        Message message = new Message (prepareNamespaceJid (extension));
        message.addExtension (extensionElement);
        message.setType (Type.normal);
        
        if (body != null && body.length () > 0) {
            message.setBody (body);
        }
        
        try { 
            connectionService.getConnection ().sendStanza (message);
            //System.out.println ("Message is sent: " + message);
        } catch (NotConnectedException | InterruptedException es) {
            new RuntimeException (es);
        }
    }
    
    @Override
    public AbsCustomExtensionElement parse (XmlPullParser parser, int initialDepth) throws Exception {
        AbsCustomExtensionElement elementExtension = null;
        
        switch (parser.getNamespace ().split ("#") [1]) {
            case "clock": elementExtension = new ClockExtension ();       break;
            case "edit" : elementExtension = new EditMessageExtension (); break;
            default: 
                throw new UnsupportedDataTypeException ();
        }
        
        elementExtension.read (parser);
        return elementExtension;
    }
    
}
