package ru.shemplo.chat.neerc.network.exten;

import javax.activation.UnsupportedDataTypeException;

import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;

import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.annot.processor.Snowball;

@Snowflake
public class CustomExtensionProvider extends ExtensionElementProvider <AbsCustomElementExtension> {

    @Cooler public static CustomExtensionProvider shapeClockExtensionProvider () {
        CustomExtensionProvider provider = new CustomExtensionProvider ();
        Snowball.runOnInited (provider::_initProvider);
        return provider;
    }
    
    static final ClockExtension clock = new ClockExtension ();
    
    private void _initProvider () {
        // x / http://neerc.ifmo.ru/protocol/neerc#clock
        ProviderManager.addExtensionProvider (clock.getElementName (), 
                                         clock.getNamespace (), this);
    }
    
    @Override
    public AbsCustomElementExtension parse (XmlPullParser parser, int initialDepth) throws Exception {
        AbsCustomElementExtension elementExtension = null;
        
        switch (parser.getNamespace ().split ("#") [1]) {
            case "clock": elementExtension = clock; break;
            default: 
                throw new UnsupportedDataTypeException ();
        }
        
        elementExtension.read (parser);
        return elementExtension;
    }
    
}
