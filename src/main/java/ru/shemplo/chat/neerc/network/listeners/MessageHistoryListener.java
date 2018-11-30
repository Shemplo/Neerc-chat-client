package ru.shemplo.chat.neerc.network.listeners;

import java.util.Set;

public interface MessageHistoryListener {
    
    /**
     * if it's empty - listen to all dialogs
     * 
     * @return
     * 
     */
    Set <String> getListeningDialogsName ();
    
    void onDialogUpdated (String dialog);
    
}
