package ru.shemplo.chat.neerc.network.control;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;

public interface PacketRouter {
    
    void addRoutersLocation (Package pkg);
    
    void route (Presence presence);
    
    void route (Message message);
    
    void route (Stanza stanza);
    
    void route (IQ iq);
    
}
