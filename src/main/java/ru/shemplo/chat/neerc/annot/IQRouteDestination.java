package ru.shemplo.chat.neerc.annot;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jivesoftware.smack.packet.IQ.Type;

@Retention (RUNTIME)
@Target ({ METHOD })
public @interface IQRouteDestination {
 
    Type meessageType () default Type.result;
    
    String name () default "query";
    
    String namespace ();
    
    String from ();
    
}
