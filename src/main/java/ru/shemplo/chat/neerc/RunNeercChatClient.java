package ru.shemplo.chat.neerc;

import javafx.application.Application;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.network.ConnectionService;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.annot.processor.Snowball;

@Slf4j
public class RunNeercChatClient extends Snowball {
    
    /*
     * TODO: change sequence of initializations
     * Now it's: Snowball.shape () -> Application.launch ()
     * Project: Application.launch () -> Snowball.shape ()
     */
    
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
        } catch (Exception | Error es) { log.error (es.getMessage ()); }
    }
    
}
