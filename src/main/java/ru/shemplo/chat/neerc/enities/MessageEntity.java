package ru.shemplo.chat.neerc.enities;

import java.time.LocalDateTime;

import lombok.*;

@Data
@EqualsAndHashCode (exclude = {"read", "time"})
@RequiredArgsConstructor
public class MessageEntity {
    
    @RequiredArgsConstructor @Getter
    public static enum MessageAccess { 
        
        PUBLIC       ("public chat"), 
        PRIVATE      ("dialog"), 
        ROOM_PRIVATE ("room chat");
        
        private final String displayName;
    
    }
    
    protected final String    dialog, 
                              ID;
    protected final LocalDateTime time;
    protected final String    author,
                              recipient;
    @NonNull protected String body;
    protected final MessageAccess access;
    
    protected boolean read;
    
}
