package ru.shemplo.chat.neerc.gfx.panes;

import static javafx.scene.layout.Priority.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.scenes.MainSceneListener;
import ru.shemplo.chat.neerc.gfx.scenes.SceneListener;
import ru.shemplo.chat.neerc.network.MessageService;
import ru.shemplo.chat.neerc.network.listeners.MessageHistoryListener;

public class Conversation extends VBox implements MessageHistoryListener {
    
    @Getter protected boolean sendingMessageEnable;
    protected final Set <String> listeningDialogs;
    @Getter protected final String dialog;
    protected final AtomicInteger unread;
    protected final ScrollPane scroller;
    protected final VBox rows;
    
    protected final MessageService messageHistory;
    protected final MainSceneListener listener;
    private final Set <MessageEntity> cache;
    
    @Getter @Setter private String input = "";
    
    public Conversation (SceneListener listener, String dialog) {
        this.listener = (MainSceneListener) listener;
        this.sendingMessageEnable = true;
        this.cache = new HashSet <> ();
        
        this.unread = new AtomicInteger (0);
        this.dialog = dialog;
        
        this.rows = new VBox ();
        rows.setSpacing (4);
        
        this.scroller = new ScrollPane (rows);
        scroller.setVbarPolicy (ScrollBarPolicy.AS_NEEDED);
        scroller.setHbarPolicy (ScrollBarPolicy.NEVER);
        scroller.setFitToWidth (true);
        scroller.setBackground (Background.EMPTY);
        scroller.setBorder (Border.EMPTY);
        setVgrow (scroller, ALWAYS);
        scroller.setVvalue (1.0d);
        
        getChildren ().add (scroller);
        
        this.listeningDialogs = new HashSet <> ();
        listeningDialogs.add (dialog);
        this.messageHistory = listener
                . getManager        ()
                . getSharedContext  ()
                . getMessageHistory ();
        messageHistory.subscribe (this);
    }
    
    public void onResponsibleTabOpened (Tab owner) {
        if (unread.get () > 0) {            
            Platform.runLater (() -> { scroller.setVvalue (1.0d); });
            Platform.runLater (() -> owner.setText (dialog));
            cache.forEach (m -> m.setRead (true));
            unread.set (0);
        }
    }
    
    @Override
    public Set <String> getListeningDialogsName () { return listeningDialogs; }

    @Override
    public void onDialogUpdated (String dialog) {
        Platform.runLater (() -> {
            Collection <MessageEntity> messages = messageHistory
                                                . getMessagesInDialog (dialog);
            Set <MessageEntity> setOfIncome = new HashSet <> (messages);
            setOfIncome.retainAll (cache);
            
            if (setOfIncome.size () != cache.size ()) { // something was deleted
                rows.getChildren ().clear ();
                cache.clear ();
            }
            
            messages.stream ()
            . filter  (m -> !cache.contains (m))
            . peek    (m -> {
                if (listener.getCurrentConversation ().equals (this)) { return; }
                unread.addAndGet (m.isRead () ? 0 : 1);
            })
            . peek    (cache::add)
            . map     (m -> new MessageRow (listener, m))
            . forEach (rows.getChildren ()::add);
            
            if (unread.get () > 0) {
                final Tab owner = listener.getOrCreateAndGetTabFor (dialog, this);
                owner.setText (String.format ("%s (%d)", dialog, unread.get ()));
            }
        });
        
        if (scroller.getVvalue () >= 0.95d) {
            try { Thread.sleep (50); } catch (InterruptedException ie) {}
            Platform.runLater (() -> { scroller.setVvalue (1.0d); });
        }
    }
    
}
