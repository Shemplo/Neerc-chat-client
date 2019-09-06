package ru.shemplo.chat.neerc.network.exten;

import static ru.shemplo.chat.neerc.network.iq.CustomIQProvider.*;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import lombok.Getter;
import lombok.ToString;
import ru.shemplo.chat.neerc.network.iq.XMLUtils;
import ru.shemplo.snowball.annot.Snowflake;

@ToString
@Snowflake
public class ClockExtension extends AbsCustomExtensionElement {

    public static enum ClockStatus {
        BEFORE, RUNNING, PAUSED, OVER
    }
    
    public ClockExtension () {
        super (String.format ("%s#clock", prepareNamespace ()), "x", "neerc");
    }
    
    @Getter private volatile ClockStatus status;
    @Getter private volatile long time, total;
    
    @Override
    public void read (XmlPullParser parser) throws XmlPullParserException, IOException {
        XMLUtils.parser2Stream (parser, getElementName ())
                .filter  (p -> "clock".equals (p.F))
                .filter  (p -> !p.S.isEmpty ())
                .forEach (p -> {
                    total = Long.parseLong (p.S.get ("total"));
                    time  = Long.parseLong (p.S.get ("time"));
                    
                    int status = Integer.parseInt (p.S.get ("status")) - 1;
                    this.status = ClockStatus.values () [status];
                });
    }
    
}
