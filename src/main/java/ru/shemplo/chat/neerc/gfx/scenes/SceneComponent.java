package ru.shemplo.chat.neerc.gfx.scenes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor (access = AccessLevel.PRIVATE)
public enum SceneComponent implements SceneContainer {
 
    ROOT ("root"),
    
    LOGIN             ("login"),
    RECONNECT_BUTTON  ("reconnect"),
    CONNECTION_ICON   ("connection_status_icon"),
    CONNECTION_STATUS ("connection_status"),
    
    USERS_SCROLL ("users_horizon"),
    USERS        ("users"),
    
    CONVERSATIONS ("conversations"),
    
    INPUT  ("input"), 
    SEND   ("send"), 
    SMILE  ("smile"),
    ATTACH ("attach"),
    
    CLOCK_TIME   ("clock_time"),
    CLOCK_TOTAL  ("clock_total"),
    CLOCK_STATUS ("clock_status"),
    
    TASKS_SCROLL ("tasks_column"),
    TASKS        ("tasks");
    
    @Getter private final String id;
    
}
