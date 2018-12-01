package ru.shemplo.chat.neerc.network.iq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.shemplo.chat.neerc.enities.TaskEntity;

public class TasksListIQ extends AbsCustomIQ {
    
    public TasksListIQ () { super ("tasks"); }

    private final List <TaskEntity> tasks = new ArrayList <> ();
    
    public Collection <TaskEntity> getTasks () {
        return Collections.unmodifiableCollection (tasks);
    }
    
    @Override
    public void read (XmlPullParser parser) throws XmlPullParserException, IOException {
        XMLUtils.parser2Stream (parser, "query")
        . filter  (p -> !p.S.isEmpty ())
        . forEach (tag -> {
            if (tag.F.equals ("task")) {
                tasks.add (TaskEntity.fromMap (tag.S));
            } else if (tag.F.equals ("status")) {
                tag.S.putIfAbsent ("value", "");
                tasks.get (tasks.size () - 1)
                . addStatus (tag.S);
            }
        });
    }
    
}
