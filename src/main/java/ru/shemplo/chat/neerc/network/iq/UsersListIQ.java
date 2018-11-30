package ru.shemplo.chat.neerc.network.iq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.shemplo.chat.neerc.enities.UserEntity;

public class UsersListIQ extends AbsCustomIQ {

    public UsersListIQ () { super ("users"); }
    
    private final List <UserEntity> users = new ArrayList <> ();
    
    public Collection <UserEntity> getUsers () {
        return Collections.unmodifiableCollection (users);
    }

    @Override
    public void read (XmlPullParser parser) throws XmlPullParserException, IOException {
        XMLUtils.parser2Stream (parser, "query")
                .filter   (p -> p.F.equals ("user"))
                .filter   (p -> !p.S.isEmpty ())
                .map      (p -> p.S)
                .map      (UserEntity::fromMap)
                .distinct ()
                .forEach  (users::add);
        
    }
    
}
