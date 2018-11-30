package ru.shemplo.chat.neerc.network.iq;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.shemplo.snowball.stuctures.Pair;

public class XMLUtils {
 
    public static Stream <Pair <String, Map <String, String>>> 
            parser2Stream (XmlPullParser parser, String stop) 
            throws XmlPullParserException, IOException {
        Stream.Builder <Pair <String, Map <String, String>>> 
            builder = Stream.builder ();
        final int end = XmlPullParser.END_TAG;
        while (true) {
            final String name = parser.getName ();
            if (parser.getEventType () == end 
                    && name.equals (stop)) { 
                break; 
            }
            
            final Map <String, String > attributes 
                = Stream.iterate  (0, i -> i + 1)
                . limit (Math.max (0, parser.getAttributeCount ()))
                . map   (i -> Pair.mp (i, i)
                        . applyF (parser::getAttributeName)
                        . applyS (parser::getAttributeValue)
                        )
                . collect (Collectors.toMap (Pair::getF, Pair::getS));
            builder.add (Pair.mp (name, attributes));
            
            parser.nextTag ();
        }
        
        return builder.build ();
    }
    
}
