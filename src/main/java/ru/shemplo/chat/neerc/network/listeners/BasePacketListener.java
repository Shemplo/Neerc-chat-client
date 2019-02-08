package ru.shemplo.chat.neerc.network.listeners;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;

import lombok.NoArgsConstructor;
import ru.shemplo.chat.neerc.network.control.PacketRouter;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
@NoArgsConstructor
public class BasePacketListener implements StanzaListener {
    
    private final List <PacketRouter> routers = new ArrayList <> ();
    
    private PacketRouter messageRouter;
    
    @Override
    public void processStanza (Stanza packet) throws NotConnectedException, 
            InterruptedException, NotLoggedInException {
        if (packet instanceof Message) {
            final Message message = (Message) packet;
            messageRouter.route (message);
            routers.forEach (r -> r.route (message));
        } else if (packet instanceof Presence) {
            final Presence presence = (Presence) packet;
            messageRouter.route (presence);
            routers.forEach (r -> r.route (presence));
        } else if (packet instanceof IQ) {
            final IQ iq = (IQ) packet;
            messageRouter.route (iq);
            routers.forEach (r -> r.route (iq));
        } else {
            messageRouter.route (packet);
            routers.forEach (r -> r.route (packet));
        }
    }
    
    public void subscribe (PacketRouter router) {
        if (messageRouter.equals (router)) { return; }
        if (routers.contains (router)) { return; }
        routers.add (router);
    }
    
}
