package ru.shemplo.chat.neerc.config;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.StanzaListener;

import lombok.Getter;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.gfx.panes.MessageInterpreter;
import ru.shemplo.chat.neerc.network.MessageService;
import ru.shemplo.chat.neerc.network.TasksService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.exten.CustomExtensionProvider;
import ru.shemplo.chat.neerc.network.iq.CustomIQProvider;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class SharedContext {
    
    @Getter private CustomExtensionProvider customExtensionProvider;
    @Getter private ConnectionListener connectionListener;
    @Getter private MessageInterpreter messageInterpreter;
    @Getter private CustomIQProvider customIQProvider;
    @Getter private StanzaListener packetListener;
    @Getter private MessageService messageHistory;
    @Getter private ClientAdapter clientAdapter;
    @Getter private ConfigStorage configStorage;
    @Getter private TasksService tasksService;
    @Getter private UsersService usersService;
    
}
