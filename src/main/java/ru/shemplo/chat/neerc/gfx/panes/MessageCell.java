package ru.shemplo.chat.neerc.gfx.panes;

import java.time.format.DateTimeFormatter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.network.UsersService;

public class MessageCell extends ListCell <MessageEntity> {
    
    private static WindowManager manager;
    private static final DateTimeFormatter DATE_FORMAT 
          = DateTimeFormatter.ofPattern ("HH:mm:ss");
    
    static {
        try {
            manager = WindowManager.getInstance ();
        } catch (InterruptedException e) {/* impossible */}
    }
    
    public MessageCell () {
        setBackground (Background.EMPTY);     
    }
    
    @Override
    protected void updateItem (MessageEntity item, boolean empty) {
        super.updateItem (item, empty || item == null);
        if (empty || item == null) { return; }
        
        final MessageInterpreter messageInterpreter = manager
            . getSharedContext ().getMessageInterpreter ();
        final UsersService usersService = manager
            . getUsersService ();
        
        final HBox line = new HBox (8.0);
        line.maxWidthProperty ().bind (getListView ().widthProperty ());
        line.getStyleClass ().add ("message-row");
        line.setPadding (new Insets (4));
        line.setMinHeight (30.0);
        
        final Label date = new Label (item.getTime ().format (DATE_FORMAT));
        date.getStyleClass ().add ("message-date");
        
        final String author = item.getAuthor ();
        Label source = new Label (author);
        if (usersService.isPower (author)) {
            source.setGraphic (messageInterpreter.getIcon ("admin", 16));
            source.setGraphicTextGap (8.0);
        }
        source.setTextFill   (usersService.getColorForName (author));
        source.getStyleClass ().add ("message-source");
        source.setAlignment  (Pos.CENTER_RIGHT);
        
        final Pane content = manager.getSharedContext ()
                           . getMessageInterpreter ()
                           . interpret (item);
        line.getChildren ().addAll (date, source, content);
        setWrapText (true);
        setGraphic (line);
    }
    
}
