package ru.shemplo.chat.neerc.enities;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode (exclude = {"read", "time"})
@RequiredArgsConstructor
public class MessageEntity {
    
    public static enum MessageAccess { PUBLIC, PRIVATE, ROOM_PRIVATE }
    
    protected final String dialog, ID;
    protected final LocalDateTime time;
    protected final String author,
                           recipient,
                           body;
    protected final MessageAccess access;
    
    protected boolean read;
    
}
