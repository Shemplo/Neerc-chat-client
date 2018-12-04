package ru.shemplo.chat.neerc.network.iq;

import org.jivesoftware.smack.packet.IQ;

public class RequestIQ extends IQ {

    public RequestIQ (String name) {
        this (name, "query");
    }
    
    protected RequestIQ (String childElementName, String childElementNamespace) {
        super (childElementNamespace, CustomIQProvider.prepareNamespace ()
                                      . concat ("#").concat (childElementName));
    }
    
    @Override
    protected IQChildElementXmlStringBuilder 
            getIQChildElementBuilder (IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket ();
        return xml;
    }
    
}
