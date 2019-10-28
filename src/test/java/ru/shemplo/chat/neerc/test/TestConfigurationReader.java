package ru.shemplo.chat.neerc.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import ru.shemplo.chat.neerc.config.ConfigStorage;

public class TestConfigurationReader {
    
    @Test
    public void test () {
        ConfigStorage storage = ConfigStorage.shapeConfigStorage ();
        assertEquals ("test", storage.get ("login").orElse (null));
    }
    
}
