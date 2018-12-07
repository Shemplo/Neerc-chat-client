package ru.shemplo.chat.neerc.annot;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention (RUNTIME)
@Target ({ PARAMETER })
public @interface DestinationValue {
    
    String value ();
    
}
