package ru.shemplo.chat.neerc.enities;

import java.time.LocalDateTime;

import org.jivesoftware.smack.util.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;

@ToString
@EqualsAndHashCode (callSuper = true)
public class PresenceMessageEntity extends MessageEntity {

    @Getter private final OnlineStatus status;
    @Getter private final String user;
    
    public PresenceMessageEntity (LocalDateTime time, String user, OnlineStatus status) {
        super ("public", StringUtils.randomString (32), 
               time, "system", "this", "presence", 
               MessageAccess.PUBLIC);
        this.status = status;
        this.user = user; 
    }
    
}
