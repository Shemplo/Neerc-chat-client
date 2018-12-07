package ru.shemplo.chat.neerc.edimes;

import java.io.File;
import java.util.Optional;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdimesPlugin implements Plugin {

    public static final String SUBDOMAIN = "extens.edimes";
    
    private ComponentManager componentManager;
    private EdimesComponent edimesComponent;
    
    @Override
    public void initializePlugin (PluginManager manager, File pluginDirectory) {
        componentManager = ComponentManagerFactory.getComponentManager ();
        edimesComponent  = new EdimesComponent (componentManager);
        
        try {
            componentManager.addComponent (SUBDOMAIN, 
                                    edimesComponent);
        } catch (ComponentException ce) {
            log.error (ce.getCause () + " " + ce.getMessage ());
        }
    }

    @Override
    public void destroyPlugin () {
        Optional.ofNullable (edimesComponent).ifPresent (component -> {
            try {
                componentManager.removeComponent (SUBDOMAIN);
            } catch (ComponentException ce) {
                log.error (ce.getCause () + " " + ce.getMessage ());
            }
        });
        
        componentManager = null;
        edimesComponent  = null;
    }
    
}
