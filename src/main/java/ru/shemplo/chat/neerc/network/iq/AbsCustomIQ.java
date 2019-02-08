package ru.shemplo.chat.neerc.network.iq;

import java.io.IOException;

import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.shemplo.chat.neerc.network.ConnectionService;

public abstract class AbsCustomIQ extends IQ {
    
    private static final ConnectionService CONNECTION_SERVICE
          = null;
    
    public AbsCustomIQ (String name) {
        this (name, "query");
    }
    
    protected AbsCustomIQ (String childElementName, String childElementNamespace) {
        super (childElementNamespace, CONNECTION_SERVICE.prepareEntityJid ()
                                      . asUnescapedString ()
                                      . concat ("#").concat (childElementName));
    }
    
    @Override
    protected IQChildElementXmlStringBuilder 
            getIQChildElementBuilder (IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket ();
        return xml;
    }
    
    /**
     * 
     */
    public abstract void read (XmlPullParser parser) throws XmlPullParserException, IOException;
    
}
