package ru.shemplo.chat.neerc.network.listeners;

import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Stanza;

import lombok.NoArgsConstructor;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
@NoArgsConstructor
public class ChatPacketsFilter implements StanzaFilter {
    
    @Override
    public boolean accept (Stanza stanza) { /* TODO: make filter */
        return true;
    }
    
}
