package ru.shemplo.chat.neerc.gfx.panes;

import static ru.shemplo.chat.neerc.enities.TaskEntity.TaskStatus.*;
import static ru.shemplo.chat.neerc.enities.TaskEntity.TaskType.*;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.shemplo.chat.neerc.enities.TaskEntity;
import ru.shemplo.chat.neerc.enities.TaskEntity.TaskStatus;
import ru.shemplo.chat.neerc.enities.TaskEntity.TaskType;
import ru.shemplo.chat.neerc.gfx.scenes.SceneListener;
import ru.shemplo.chat.neerc.network.iq.RequestIQ;
import ru.shemplo.chat.neerc.network.iq.TaskStatusIQ;

public class TaskTile extends VBox {
    
    private final MessageInterpreter interpreter;
    private final SceneListener listener;
    private final Button status, confirm;
    private final TextField comment;

    private TaskStatus taskStatus;
    private final TaskType type;
    private final String taskID;
    
    private String beforeTyping;
    
    public TaskTile (SceneListener listener, TaskEntity task) {
        this.interpreter = listener.getManager   ()
                         . getSharedContext      ()
                         . getMessageInterpreter ();
        this.taskID = task.getId ();
        this.type = task.getType ();
        this.listener = listener;
        
        setOnMouseClicked (me -> {
            if (me.getClickCount () == 2) { 
                this.changeStatusByClicking ();
            }
        });
        
        HBox firstLine = new HBox (8.0);
        getChildren ().add (firstLine);
        
        Label title = new Label (task.getTitle ());
        HBox.setHgrow (title, Priority.ALWAYS);
        firstLine.getChildren ().add (title);
        
        HBox secondLine = new HBox (8.0);
        secondLine.setAlignment (Pos.CENTER_LEFT);
        getChildren ().add (secondLine);
        
        status = new Button ();
        status.setDisable (type.equals (QUESTION));
        status.getStyleClass ().add ("task-button");
        secondLine.getChildren ().addAll (status, 
           new Separator (Orientation.VERTICAL));
        status.setOnMouseClicked (__ -> changeStatusByClicking ());
        
        boolean disableText  = type.equals (CONFIRM) 
                            || type.equals (TODO);
        
        comment = new TextField ();
        comment.setDisable (disableText);
        HBox.setHgrow (comment, Priority.ALWAYS);
        secondLine.getChildren ().add (comment);
        comment.setOnKeyPressed (__ -> {
            if (beforeTyping == null) {
                beforeTyping = comment.getText ();
            }
        });
        
        confirm = new Button ();
        confirm.getStyleClass ().add ("task-button");
        confirm.setDisable (true); // Field is empty
        confirm.setGraphic (interpreter.getIcon ("save", 16));
        secondLine.getChildren ().add (confirm);
        confirm.setOnMouseClicked (__ -> {
            final String input = comment.getText ();
            beforeTyping = null;
            
            sendTextRequiredStatus (input);
        });
        
        comment.setOnKeyReleased (ke -> {
            final String input = comment.getText ();
            boolean flag = beforeTyping == null || input.equals (beforeTyping);
            if (KeyCode.ENTER.equals (ke.getCode ()) && !flag) {
                sendTextRequiredStatus (input);
            } else {                
                Platform.runLater (() -> 
                    confirm.setDisable (flag));
            }
        });
        
        final String user = listener.getManager ().getConfigStorage ()
                          . get ("login").orElse ("[user name]");
        task.getStatusFor (user).ifPresent (status -> {
            changeStatusTo (status.F, status.S);
        });
    }
    
    private void changeStatusByClicking () {
        RequestIQ requestIQ = null;
        if (type.equals (CONFIRM)) {
            requestIQ = new TaskStatusIQ (taskID, 
                taskStatus.equals (SUCCESS) ? NONE: SUCCESS, "");
        } else if (type.equals (TODO) || type.equals (TODOFAIL)) {
            boolean isS = taskStatus.equals (SUCCESS), isR = taskStatus.equals (RUNNING), 
                    isF = taskStatus.equals (FAIL);
            requestIQ = new TaskStatusIQ (taskID, isS ? RUNNING 
                      : ((isR || isF) ? SUCCESS : RUNNING), "");
        } else if (type.equals (OKFAIL) && !taskStatus.equals (SUCCESS)) {
            requestIQ = new TaskStatusIQ (taskID, SUCCESS, "");
        }
        
        if (requestIQ != null) {
            listener.getManager ().getSharedContext ().getCustomIQProvider ()
                    .send (requestIQ);
            
            if (requestIQ instanceof TaskStatusIQ) {
                TaskStatusIQ tmp = (TaskStatusIQ) requestIQ;
                changeStatusTo (tmp.getStatus (), tmp.getValue ());
            }
        }
    }
    
    private void sendTextRequiredStatus (String input) {
        if (input.length () == 0) { return; }
        
        Platform.runLater (() -> confirm.setDisable (true));
        
        TaskStatus status = type.equals (QUESTION)
                ? TaskStatus.SUCCESS
                : TaskStatus.FAIL;
        
        RequestIQ requestIQ = new TaskStatusIQ (taskID, status, input);
        listener.getManager ().getSharedContext ().getCustomIQProvider ()
              .send (requestIQ);
        
        changeStatusTo (status, input);
    }
    
    public void changeStatusTo (TaskStatus status, String value) {
        String icon = String.format ("task/%s", status.name ().toLowerCase ());
        this.taskStatus = status;
        Platform.runLater (() -> {
            this.status.setGraphic (interpreter.getIcon (icon, 16));
            this.comment.setText (value);            
        });
    }
    
}
