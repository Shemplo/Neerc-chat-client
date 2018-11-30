package ru.shemplo.chat.neerc.annot;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jivesoftware.smack.packet.Message.Type;


@Retention (RUNTIME)
@Target ({ METHOD })
public @interface MessageRouteDestination {
    
    Type meessageType () default Type.groupchat;
    
    boolean wisper () default false;
    
    String author () default ".*";
    
    String namespace ();
    
    String room ();
    
    boolean roomExpectation () default true;
    
}
