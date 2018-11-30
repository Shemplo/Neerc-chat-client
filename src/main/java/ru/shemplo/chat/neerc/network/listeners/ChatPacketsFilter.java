package ru.shemplo.chat.neerc.network.listeners;

import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class ChatPacketsFilter implements StanzaFilter {

    @Cooler public static ChatPacketsFilter shapeChatPacketFilter () {
        return new ChatPacketsFilter ();
    }
    
    @Override
    public boolean accept (Stanza stanza) { /* TODO: make filter */
        return true;
    }
    
}
