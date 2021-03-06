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
    
    USERS_SCROLL_H ("users_horizon_h"),
    USERS_SCROLL_V ("users_horizon_v"),
    USERS_H        ("users_h"),
    USERS_V        ("users_v"),
    JOIN_ROOM      ("join_room"),
    ROOM_NAME      ("room_name"),
    
    CONVERSATIONS ("conversations"),
    
    BUFFER       ("buffer_content"),
    CLEAR_BUFFER ("clear_buffer"),
    
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
