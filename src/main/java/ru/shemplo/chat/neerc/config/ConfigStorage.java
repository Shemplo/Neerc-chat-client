package ru.shemplo.chat.neerc.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.utils.StringManip;

@Snowflake
public class ConfigStorage {
    
    @Cooler public static ConfigStorage shapeConfigStorage () {
        return new ConfigStorage ();
    }
    
    private final Map <String, String> values = new HashMap <> ();
    
    private ConfigStorage () {
        final String configName = Optional
                                . ofNullable (System.getProperty ("config"))
                                . orElse ("config.conf");
        Path configPath = Paths.get (configName).toAbsolutePath ();
        if (!Files.exists (configPath)) {
            String message = String.format ("Config file `%s` doesn't exist", 
                                            configName);
            throw new IllegalStateException (message);
        }
        
        try (
            BufferedReader br = Files.newBufferedReader (configPath);
        ) {
            String line = null;
            while ((line = StringManip.fetchNonEmptyLine (br)) != null) {
                final String [] parts = line.split ("=");
                for (int i = 0; i < parts.length; i++) {
                    parts [i] = parts [i].trim ();
                }
                values.put (parts [0], parts [1]);
            }
        } catch (IOException ioe) { throw new RuntimeException (ioe); }
    }
    
    public Optional <String> get (String key) {
        return Optional.ofNullable (values.get (key));
    }
    
    public <R> Optional <R> get (String key, Function <String, R> converter) {
        return get (key).map (converter);
    }
    
    public boolean printDebug () {
        Optional <Boolean> value = get ("debug", Boolean::parseBoolean);
        return value.isPresent ()
             ? value.get ()
             : false;
    }
    
}
