package ru.shemplo.chat.neerc.network.control;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;

import lombok.extern.slf4j.Slf4j;
import ru.shemplo.chat.neerc.annot.DestinationValue;
import ru.shemplo.chat.neerc.annot.IQRouteDestination;
import ru.shemplo.chat.neerc.annot.MessageRouteDestination;
import ru.shemplo.chat.neerc.annot.PresenceRouteDestination;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.annot.processor.Snowball;
import ru.shemplo.snowball.stuctures.Pair;
import ru.shemplo.snowball.utils.ClasspathUtils;

@Slf4j
@Snowflake
public class DefaultPacketRouter extends AbsPacketRouter {
    
    @Cooler public static DefaultPacketRouter shapeDefaultMessageRouter () {
        DefaultPacketRouter packetRouter = new DefaultPacketRouter ();
        return packetRouter;
    }
    
    @Init private ClientAdapter clientAdapter;
    @Init private ConfigStorage configStorage;
    
    private final Map <Class <? extends Annotation>, List <Method>> 
        ROUTE_METHODS = new HashMap <> ();
    
    public DefaultPacketRouter () {
        Package routerPackage = PacketRouter.class.getPackage ();
        Set <Class <? extends Annotation>> annotations 
          = Arrays.asList (MessageRouteDestination.class, IQRouteDestination.class,
                           PresenceRouteDestination.class)
          . stream ().collect (Collectors.toSet ());
        ClasspathUtils.findAllAnnotations (routerPackage, annotations)
                      .entrySet ().stream ().map (Pair::fromMapEntry)
                      .map (p -> p.applyS (
                              list -> list.stream ().map (o -> (Method) o)
                              /*
                                    . filter (m -> Modifier.isPublic (m.getModifiers ())
                                                && Modifier.isStatic (m.getModifiers ()))
                                                */
                                    . collect (Collectors.toList ())
                           ))
                      .forEach (p -> ROUTE_METHODS.put (p.F, p.S));
    }
    
    @Override
    public void addRoutersLocation (Package pkg) {
        
    }
    
    @Override
    public void route (Message message) {
        if (message.getBody () == null) { return; }
        
        final String from = message.getFrom ().asUnescapedString (),
                     to   = message.getTo ().asUnescapedString ();
        final String [] toParts = to.split ("[@/]");
        final String recipient = toParts [0];
        
        String tmpBody = message.getBody ();
        final String [] fromParts = from.split ("[@/]");
        final String namespace = from.contains ("@") 
                               ? fromParts [1] : fromParts [0], 
                     author    = from.contains ("/") 
                               ? fromParts [fromParts.length - 1] : "";
        String tmpRoom   = from.contains ("@") ? fromParts [0] : "", 
               tmpWisper = "";
        
        final int angle = tmpBody.indexOf (">");
        if (tmpBody.matches ("\\%\\w+>(.|\\n)+")) {     // like "%bot> connect"
            tmpRoom  = tmpBody.substring (1, angle);
            tmpBody = tmpBody.substring (angle + 1);
        } else if (tmpBody.matches ("\\w+>(.|\\n)+")) { // like "hall1> Hello"
            tmpWisper = tmpBody.split (">") [0];
            tmpBody = tmpBody.substring (angle + 1);
        }
        tmpBody = tmpBody.trim ();
        
        final Class <? extends MessageRouteDestination> routeAnnotation = MessageRouteDestination.class;
        final String room = tmpRoom, wisper = tmpWisper, body = tmpBody;
        final LocalDateTime time = LocalDateTime.now ();
        
        Optional <Method> callMethod = ROUTE_METHODS.get (routeAnnotation).stream ()
                                     . map (m -> Pair.mp (m, m.getAnnotation (routeAnnotation)))
                                     . filter (p -> room.matches   (p.S.room ()) == p.S.roomExpectation ())
                                     . filter (p -> namespace.matches (p.S.namespace ()))
                                     . filter (p -> p.S.wisper () != (wisper.isEmpty ()))
                                     . filter (p -> author.matches (p.S.author ()))
                                     . map (p -> p.F)
                                     . findFirst ();
        
        final Map <String, ? super Object> values = new HashMap <> ();
        values.put ("recipient", wisper.isEmpty () ? recipient : wisper);
        values.put ("id", message.getStanzaId ());
        values.put ("source", namespace);
        values.put ("message", message);
        values.put ("author", author);
        values.put ("wisper", wisper);
        values.put ("body", body);
        values.put ("room", room);
        values.put ("time", time);
        
        callMethod (callMethod, values);
    }
    
    @Override
    public void route (Presence presence) {
        //MUCUser user = presence.getExtension (MUCUser.ELEMENT, MUCUser.NAMESPACE);
        //if (user == null) { return; }
        //System.out.println (presence);
        //log.debug (String.format ("extension: %s", user.getItem ().getRole ()));
        //log.debug (String.format ("available: %s", presence.isAvailable ()));
        final String from = presence.getFrom ().asUnescapedString ();
        final String [] fromParts = from.split ("[@/]");
        final String room      = fromParts [0], 
                     namespace = fromParts [1], 
                     author    = fromParts [2];
        //log.debug (String.format ("login: %s, %s, %s", room, namespace, author));
        
        final Class <? extends PresenceRouteDestination> 
            routeAnnotation = PresenceRouteDestination.class;
        final LocalDateTime time = LocalDateTime.now ();
        
        Optional <Method> callMethod = ROUTE_METHODS.get (routeAnnotation).stream ()
                                     . map (m -> Pair.mp (m, m.getAnnotation (routeAnnotation)))
                                     . filter (p -> namespace.matches (p.S.namespace ()))
                                     . filter (p -> author.matches (p.S.author ()))
                                     . filter (p -> room.matches   (p.S.room ()))
                                     . map (p -> p.F)
                                     . findFirst ();
        
        final Map <String, ? super Object> values = new HashMap <> ();
        values.put ("available", presence.isAvailable ());
        values.put ("presence", presence);
        values.put ("source", namespace);
        values.put ("author", author);
        values.put ("room", room);
        values.put ("time", time);
        
        callMethod (callMethod, values);
    }
    
    @Override
    public void route (IQ iq) {
        //iq.getChildElementNamespace (); // neerc@conference.[host]#users
        //iq.getFrom ();                  // neerc@neerc.[host]
        //iq.getChildElementName ();      // query
        //iq.getType ();                  // result | get | set
        
        final String namespace = Optional.ofNullable (iq.getChildElementNamespace ()).orElse (""),
                     from      = iq.getFrom () == null ? "" : iq.getFrom ().asUnescapedString (),
                     name      = Optional.ofNullable (iq.getChildElementName ()).orElse ("");
        final LocalDateTime time = LocalDateTime.now ();
        final Type type = iq.getType ();
        
        final Class <? extends IQRouteDestination> routeAnnotation = IQRouteDestination.class;
        Optional <Method> callMethod = ROUTE_METHODS.get (routeAnnotation).stream ()
                                     . map (m -> Pair.mp (m, m.getAnnotation (routeAnnotation)))
                                     . filter (p -> type.equals (p.S.meessageType ()))
                                     . filter (p -> name.matches   (p.S.name ()))
                                     . filter (p -> namespace.matches (p.S.namespace ()))
                                     . filter (p -> from.matches (p.S.from ()))
                                     . map (p -> p.F)
                                     . findFirst ();
        
        final Map <String, ? super Object> values = new HashMap <> ();
        values.put ("namespace", namespace);
        values.put ("from", from);
        values.put ("name", name);
        values.put ("time", time);
        values.put ("type", type);
        values.put ("iq", iq);
        
        callMethod (callMethod, values);
    }

    @Override
    public void route (Stanza stanza) {
        log.debug ("Stanza: " + stanza);
    }
    
    private final void callMethod (Optional <Method> callMethod, 
            Map <String, ? super Objects> parameters) {
        callMethod.ifPresent (method -> {
            final Class <? extends DestinationValue> destAnnotation = DestinationValue.class;
            final Object context = Snowball.getSnowflakeFor (method.getDeclaringClass ());
            
            try {
                Object [] arguments = Arrays.stream (method.getParameters ())
                                    . map     (t -> t.getAnnotation (destAnnotation))
                                    . filter  (Objects::nonNull)
                                    . map     (DestinationValue::value)
                                    . map     (parameters::get)
                                    . collect (Collectors.toList ())
                                    . toArray ();
                method.invoke (context, arguments);
            } catch (IllegalAccessException | IllegalArgumentException 
                  | InvocationTargetException __) {
                // TODO: handle exception
            }
        });
        
        if (!callMethod.isPresent () && configStorage.printDebug ()) {
            log.warn ("Controller not found for message: " + parameters.get ("room"));
        }
    }
    
}
