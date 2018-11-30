package ru.shemplo.chat.neerc.network.iq;

import lombok.Getter;
import ru.shemplo.chat.neerc.enities.TaskEntity.TaskStatus;

public class TaskStatusIQ extends RequestIQ {

    @Getter private final String taskID, value;
    @Getter private final TaskStatus status; 
    
    public TaskStatusIQ (String taskID, TaskStatus status, String value) {
        super ("taskstatus", "taskstatus");
        
        this.status = status;
        this.taskID = taskID; 
        this.value = value;
    }
    
    @Override
    protected IQChildElementXmlStringBuilder 
            getIQChildElementBuilder (IQChildElementXmlStringBuilder xml) {
        xml.attribute ("type", status.name ().toLowerCase ());
        xml.attribute ("value", value);
        xml.attribute ("id", taskID);
        xml.rightAngleBracket ();
        
        return xml;
    }
    
}
