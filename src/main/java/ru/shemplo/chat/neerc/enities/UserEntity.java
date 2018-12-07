package ru.shemplo.chat.neerc.enities;

import java.util.Map;
import java.util.Objects;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class UserEntity {
    
    public static enum UserPower { PARTICIPANT, POWER }
    
    public static enum OnlineStatus { OFFLINE, ONLINE }
    
             private OnlineStatus  status = OnlineStatus.OFFLINE;
    @NonNull private final String    name;
    @NonNull private       String    group;
    @NonNull private       UserPower power;
    
    public static UserEntity fromMap (Map <String, String> map) {
        Objects.requireNonNull (map.get ("name") );
        Objects.requireNonNull (map.get ("group"));
        Objects.requireNonNull (map.get ("power"));
        
        return new UserEntity (map.get ("name"), map.get ("group"),
                               "yes".equals (map.get ("power"))
                               ? UserPower.POWER : UserPower.PARTICIPANT);
    }
    
}
