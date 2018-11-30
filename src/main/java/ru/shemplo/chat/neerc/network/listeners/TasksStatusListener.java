package ru.shemplo.chat.neerc.network.listeners;


public interface TasksStatusListener {
    
    void onTasksUpdated ();
    
    void onTaskUpdated (String id);
    
}
