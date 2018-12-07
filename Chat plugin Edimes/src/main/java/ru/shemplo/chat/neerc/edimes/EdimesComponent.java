package ru.shemplo.chat.neerc.edimes;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;
import org.xmpp.packet.Packet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdimesComponent implements Component {

    private final ComponentManager componentManager;
    private final MultiUserChatService MUCService;
    private final String DOMAIN, ROOM;
    
    public EdimesComponent (ComponentManager componentManager) {
        XMPPServer server = XMPPServer.getInstance ();
        this.componentManager = componentManager;
        
        this.MUCService = server.getMultiUserChatManager ()
                                . getMultiUserChatServices ()
                                . get (0);
        this.DOMAIN = String.format ("%s.%s", EdimesPlugin.SUBDOMAIN, 
                                  componentManager.getServerName ());
        this.ROOM = "neerc";
    }
    
    @Override
    public String getName () {
        return "Edimes chat plugin";
    }

    @Override
    public String getDescription () {
        return "Additional manipulations with messages";
    }

    @Override
    public void processPacket (Packet packet) {
        if (packet instanceof Message) {
            final Message input = (Message) packet;
            
            final String author = input.getFrom ().getNode ();
            input.setFrom (String.format ("%s@%s/%s", ROOM, DOMAIN, author));
            input.setBody (String.format ("%s just edited message", author));
            input.setType (Type.groupchat);
            
            for (MUCRoom room : MUCService.getChatRooms ()) {            
                for (MUCRole role : room.getOccupants ()) {
                    role.send (input);
                }
            }
        }
    }
    
    @SuppressWarnings ("unused")
    private void sendPacket (Packet packet) {
        try {
            componentManager.sendPacket (this, packet);
        } catch (ComponentException ce) {
            log.error (ce.getMessage ());
        }
    }

    @Override
    public void initialize (JID jid, ComponentManager componentManager) 
            throws ComponentException {
        
    }

    @Override
    public void start () {}

    @Override
    public void shutdown () {}
    
}
