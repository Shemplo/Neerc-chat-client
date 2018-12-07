package ru.shemplo.chat.neerc.network.exten.editor;

import java.io.IOException;
import java.util.Objects;

import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import lombok.Getter;
import lombok.ToString;
import ru.shemplo.chat.neerc.network.exten.AbsCustomExtensionElement;
import ru.shemplo.chat.neerc.network.iq.XMLUtils;

@ToString
public class EditMessageExtension extends AbsCustomExtensionElement {

    public static enum EditActionType {
        EDIT, DELETE, QUOTE
    }
    
    public static final String NAMESPACE = "http://shemplo.ru/edimes#edit", 
                               NAME      = "edition", 
                               EXTENSION = "extens.edimes";
    
    public EditMessageExtension () {
        super (NAMESPACE, NAME, EXTENSION);
    }
    
    @Getter private EditActionType actionType;
    @Getter private String messageID, value;
    
    public EditMessageExtension (String id, EditActionType actionType, String value) {
        super (NAMESPACE, NAME, EXTENSION);
        Objects.requireNonNull (actionType);
        Objects.requireNonNull (id);
        
        this.actionType = actionType;
        this.messageID  = id;
        this.value      = value;
    }
    
    @Override
    public CharSequence toXML (String namespace) {
        Objects.requireNonNull (actionType);
        //Objects.requireNonNull (messageID);
        
        return new XmlStringBuilder (this)
             . rightAngleBracket ()
             . halfOpenElement ("message")
             . attribute ("id",    messageID)
             . attribute ("type",  actionType.name ())
             . attribute ("value", value == null ? "" : value)
             . closeEmptyElement ()
             . closeElement (getElementName ());
    }

    @Override
    public void read (XmlPullParser parser) throws XmlPullParserException, IOException {
        XMLUtils.parser2Stream (parser, getElementName ())
                .filter  (p -> "message".equals (p.F))
                .filter  (p -> !p.S.isEmpty ())
                .forEach (p -> {
                    this.messageID  = p.S.get ("id");
                    this.value      = p.S.get ("value");
                    this.actionType = EditActionType.valueOf (p.S.get ("type"));
                });
    }
    
}
