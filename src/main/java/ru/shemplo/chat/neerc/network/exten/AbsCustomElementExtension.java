package ru.shemplo.chat.neerc.network.exten;

import java.io.IOException;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbsCustomElementExtension implements ExtensionElement {
    
    @Getter private final String namespace, elementName;
    
    @Override
    public CharSequence toXML (String namespace) {
        return new XmlStringBuilder (this)
             . rightAngleBracket () ////////////
             . closeElement (getElementName ());
    }
    
    /**
     * 
     */
    public abstract void read (XmlPullParser parser) throws XmlPullParserException, IOException;
    
}
