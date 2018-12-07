package ru.shemplo.chat.neerc.network.control;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;

public abstract class AbsPacketRouter implements PacketRouter {
    
    @Override
    public void route (Presence presence) {}
    
    @Override
    public void route (Message message) {}
    
    @Override
    public void route (Stanza stanza) {}
    
    @Override
    public void route (IQ iq) {}
    
}
