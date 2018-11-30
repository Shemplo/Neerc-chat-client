package ru.shemplo.chat.neerc.network.listeners;


public interface ConnectionStatusListener {
 
    void onConnectionStatusChanged (ConnectionStatus status, String message);
    
}
