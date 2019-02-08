package ru.shemplo.chat.neerc;

import javafx.application.Application;
import lombok.Getter;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.gfx.panes.MessageInterpreter;
import ru.shemplo.chat.neerc.network.ConnectionService;
import ru.shemplo.chat.neerc.network.MessageService;
import ru.shemplo.chat.neerc.network.control.PacketRouter;
import ru.shemplo.chat.neerc.network.exten.CustomExtensionProvider;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.annot.Wind;
import ru.shemplo.snowball.annot.processor.Snowball;

@Wind (blow = {PacketRouter.class, ConfigStorage.class, MessageService.class, 
               MessageInterpreter.class, CustomExtensionProvider.class,
               WindowManager.class})
public class RunNeercChatClient extends Snowball {
    
    public static void main (String... args) { shape (args); }
    
    private ConnectionService connectionService;
    private ClientAdapter clientAdapter;
    
    @Snowflake (manual = true)
    @Getter private WindowManager window;
    
    @Override
    protected void onShaped (String ... args) {
        new Thread (() -> Application.launch (WindowManager.class),
                    "Main-Window-Thread").start ();
        try {
            this.window = WindowManager.getInstance ();
            clientAdapter.clientWindowInitialized (window);
            
            while (!window.isInitialized ()) {} // Waiting for initialization of graphics
            connectionService.connect (); /* First auto connection */
        } catch (Exception es) { es.printStackTrace(); }
    }
    
}
