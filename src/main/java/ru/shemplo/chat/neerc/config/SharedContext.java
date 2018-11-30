package ru.shemplo.chat.neerc.config;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.StanzaListener;

import lombok.Getter;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.gfx.panes.MessageInterpreter;
import ru.shemplo.chat.neerc.network.MessageService;
import ru.shemplo.chat.neerc.network.TasksService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.iq.CustomIQProvider;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class SharedContext {
    
    @Cooler public static SharedContext shapeShatedContext () {
        return new SharedContext ();
    }
    
    @Getter @Init private ConnectionListener connectionListener;
    @Getter @Init private MessageInterpreter messageInterpreter;
    @Getter @Init private CustomIQProvider customIQProvider;
    @Getter @Init private StanzaListener packetListener;
    @Getter @Init private MessageService messageHistory;
    @Getter @Init private ClientAdapter clientAdapter;
    @Getter @Init private ConfigStorage configStorage;
    @Getter @Init private TasksService tasksService;
    @Getter @Init private UsersService usersService;
    
}
