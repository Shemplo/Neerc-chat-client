package ru.shemplo.chat.neerc.network.iq;

import org.jivesoftware.smack.packet.IQ;

import ru.shemplo.snowball.annot.processor.Snowball;

public class RequestIQ extends IQ {
    
    private static final CustomIQProvider CUSTOM_IQ_PROVIDER
          = Snowball.getSnowflakeFor (CustomIQProvider.class);

    public RequestIQ (String name) {
        this (name, "query");
    }
    
    protected RequestIQ (String childElementName, String childElementNamespace) {
        super (childElementNamespace, CUSTOM_IQ_PROVIDER.prepareNamespace ()
                                      . concat ("#").concat (childElementName));
    }
    
    @Override
    protected IQChildElementXmlStringBuilder 
            getIQChildElementBuilder (IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket ();
        return xml;
    }
    
}
