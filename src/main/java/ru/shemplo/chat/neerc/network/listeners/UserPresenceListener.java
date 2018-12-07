package ru.shemplo.chat.neerc.network.listeners;

import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;

public interface UserPresenceListener {

    void onUserChangedPresence (String user, OnlineStatus status);
    
    void onUsersUpdated ();
    
}
