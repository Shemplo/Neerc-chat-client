package ru.shemplo.chat.neerc.network;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.enities.TaskEntity;
import ru.shemplo.chat.neerc.network.iq.CustomIQProvider;
import ru.shemplo.chat.neerc.network.listeners.TasksStatusListener;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class TasksService {
    
    @Cooler public static TasksService shapeTasksService () {
        return new TasksService ();
    }
    
    @Init private CustomIQProvider customIQProvider;
    @Init private ConfigStorage configStorage;
    
    private final Object STUB_OBJECT = new Object ();
    private final ConcurrentMap <TasksStatusListener, Object> 
        listeners = new ConcurrentHashMap <> ();
    
    public void subscribe (final TasksStatusListener listener) {
        listeners.computeIfAbsent (listener, k -> {
            listener.onTasksUpdated ();
            return STUB_OBJECT;
        });
    }
    
    private final Map <String, TaskEntity> 
        tasks = new ConcurrentHashMap <> ();
    
    public synchronized void mergeTasks (Collection <TaskEntity> tasks) {        
        boolean numberOfTasksChanged = this.tasks.size () != tasks.size ();
        List <String> keys = new ArrayList <> (this.tasks.keySet ());
        Set <String> keep = new HashSet <> (tasks.stream ()
                          . map (TaskEntity::getId)
                          . collect (Collectors.toSet ()));
        keep.retainAll (this.tasks.keySet ());
        keys.stream ().filter (k -> !keep.contains (k))
            .forEach (this.tasks::remove);
        
        AtomicBoolean hasJustAssignedTask = new AtomicBoolean (false);
        tasks.forEach (task -> {
            this.tasks.compute (task.getId (), (k, v) -> {
                if (v == null) {
                    hasJustAssignedTask.set (true);
                    return task;
                }
                
                if (v.getStatuses ().size () == 0) {
                    hasJustAssignedTask.set (true);
                }
                
                // TODO: hash compare
                
                return task;
            });
        });
        
        if (numberOfTasksChanged || hasJustAssignedTask.get ()) {
            listeners.keySet ().forEach (TasksStatusListener::onTasksUpdated);
        } else {
            tasks.forEach (task -> {
                listeners.keySet ().forEach (lis -> 
                    lis.onTaskUpdated (task.getId ()));
            });
        }
    }
    
    public Collection <TaskEntity> getActualTasksFor (String name) {
        return tasks.values ().stream ()
             . filter  (task -> task.isAssignedTo (name))
             . collect (Collectors.toList ());
    }
    
    public Optional <TaskEntity> getTaskByID (String id) {
        return Optional.ofNullable (tasks.get (id));
    }
    
    public void onTaskChanged (String taskID) {
        System.out.println ("Task `" + taskID + "` changed");
        customIQProvider.query ("tasks");
    }
    
}
