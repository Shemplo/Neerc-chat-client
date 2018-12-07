package ru.shemplo.chat.neerc.enities;

import static java.time.LocalDateTime.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.shemplo.snowball.stuctures.Pair;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class TaskEntity {
    
    public static enum TaskType { TODO, TODOFAIL, CONFIRM, OKFAIL, QUESTION }
    
    public static enum TaskStatus { NONE, RUNNING, SUCCESS, FAIL, ACKNOWLEDGED } // fixed by @egormkn
    
    @NonNull private final String        id;
    @NonNull private       TaskType      type;
    @NonNull private       String        title;
    @NonNull private       LocalDateTime time;
    
    private final ConcurrentMap <String, Pair <TaskStatus, String>>
        statuses = new ConcurrentHashMap <> ();
    
    public boolean isAssignedTo (String name) {
        return statuses.containsKey (name);
    }
    
    public Optional <Pair <TaskStatus, String>> getStatusFor (String name) {
        return Optional.ofNullable (statuses.get (name));
    }
    
    public static TaskEntity fromMap (Map <String, String> map) {
        Objects.requireNonNull (map.get ("timestamp"));
        Objects.requireNonNull (map.get ("title"));
        Objects.requireNonNull (map.get ("type"));
        Objects.requireNonNull (map.get ("id"));
        
        Long timestamp = Long.parseLong (map.get ("timestamp")) / 1000;
        return new TaskEntity (map.get ("id"), TaskType.valueOf (map.get ("type").toUpperCase ()), 
                               map.get ("title"), ofEpochSecond (timestamp, 0, ZoneOffset.UTC));
    }
    
    public void addStatus (Map <String, String> map) {
        Objects.requireNonNull (map.get ("value"));
        Objects.requireNonNull (map.get ("type"));
        Objects.requireNonNull (map.get ("for"));
        
        statuses.computeIfAbsent (map.get ("for"), k -> {
            TaskStatus status = TaskStatus.valueOf (map.get ("type").toUpperCase ());
            return Pair.mp (status, map.get ("value"));
        });
    }
    
}
