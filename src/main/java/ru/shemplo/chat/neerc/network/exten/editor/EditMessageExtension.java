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

/**
 * <b>Extension:</b> SXS-0001 (<i>Edimes</i>).
 * 
 * This extension defines protocol of editing messages that was already sent.
 * 
 * <h3>Motivation:</h3>
 * In default XMPP protocol there are no opportunities to change body of sent
 * message (f.e. mistakes correction) or even delete message from chat scroll.
 * The usability of this features doesn't need to be commented, so that's why
 * this extension appeared.
 * 
 * <h3>Packet frame</h3>
 * This section describes packet that had to be sent to server or could be
 * received as an message from server.
 * Example of packet frame:
 * 
 * <pre>
 * &lt;edition xmlns="http://shemplo.ru/edimes#edit"&gt;
 *   &lt;message id="AAAA-0000" type="EDIT" value="..." /&gt;
 * &lt;/edition&gt;
 * </pre>
 * 
 * Root element has name <i>edition</i> and namespace as in example.
 * Then in it's children there is single empty element with name <i>message</i>.
 * It definitely has 3 attributes: <i>id</i>, <i>type</i>, <i>value</i>.
 * <ul>
 *   <li>
 *     <b>id</b> - field that defines aimed message in chat scroll.
 *     This is required field and it can't have empty or <i>null</i> value.
 *     It can be fetched with method {@link #getMessageID()}.
 *   </li>
 *   <li>
 *     <b>type</b> - field that defines what action should be done on aimed message.
 *     This is required field and it can't have empty or <i>null</i> value.
 *     It can be fetched with method {@link #getActionType()}.
 *     Detailed explanation of actions is described in section <b>Available actions</b>.
 *   </li>
 *   <li>
 *     <b>value</b> - additional filed for providing task-dependent content.
 *     This filed is not required and can be empty or has <i>null</i> value.
 *     It can be fetched with method {@link #getValue()}.
 *   </li>
 * </ul>
 * 
 * <h3>Available actions</h3>
 * This section describes second filed (<i>type</i>) of <i>message</i> element.
 * All this actions should be interpreted as 'binding' and can't be ignored.
 * 
 * <ul>
 *   <li>
 *     <b>EDIT</b> - this action signals that body of specified message must
 *     be changed to another body (that follows in filed <i>value</i> of this
 *     packet).
 *   </li>
 *   <li>
 *     <b>DELETE</b> - this action signals that aimed message must be deleted
 *     from chat scroll (or made invisible at least).
 *   </li>
 *   <li>
 *      <b>QUOTE</b> - unspecified yet.
 *   </li>
 * </ul>
 * 
 * @author Shemplo
 *
 */
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
