package ru.shemplo.chat.neerc.network.control;

import static ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess.*;

import java.time.LocalDateTime;
import java.util.Optional;

import ru.shemplo.chat.neerc.annot.DestinationValue;
import ru.shemplo.chat.neerc.annot.IQRouteDestination;
import ru.shemplo.chat.neerc.annot.MessageRouteDestination;
import ru.shemplo.chat.neerc.annot.PresenceRouteDestination;
import ru.shemplo.chat.neerc.config.ConfigStorage;
import ru.shemplo.chat.neerc.enities.MessageEntity;
import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;
import ru.shemplo.chat.neerc.gfx.ClientAdapter;
import ru.shemplo.chat.neerc.network.MessageService;
import ru.shemplo.chat.neerc.network.TasksService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.iq.CustomIQProvider;
import ru.shemplo.chat.neerc.network.iq.TasksListIQ;
import ru.shemplo.chat.neerc.network.iq.UsersListIQ;
import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Snowflake;

@Snowflake
public class DefaultController {
    
    @Cooler public static DefaultController shapeDefaultController () {
        return new DefaultController ();
    }
    
    @Init private CustomIQProvider customIQProvider;
    @Init private MessageService messageHistory;
    @Init private ClientAdapter clientAdapter;
    @Init private ConfigStorage configStorage;
    @Init private TasksService tasksService;
    @Init private UsersService usersService;
    
    @MessageRouteDestination (namespace = "conference\\..+", room = "neerc")
    public void controllPublicChatMessage (
            @DestinationValue ("body")   String        body,
            @DestinationValue ("time")   LocalDateTime time,
            @DestinationValue ("author") String        author,
            @DestinationValue ("id")     String        id) {
        //System.out.println (String.format ("public `%s: %s`", author, body));
        final String login = configStorage.get ("login").get ();
        MessageEntity message = new MessageEntity ("public", id, time, 
                                         author, login, body, PUBLIC);
        messageHistory.addMessage (message);
    }
    
    @MessageRouteDestination (namespace = "conference\\..+", room = "neerc", wisper = true)
    public void controllWisperChatMessage (
            @DestinationValue ("body")      String        body,
            @DestinationValue ("time")      LocalDateTime time,
            @DestinationValue ("author")    String        author,
            @DestinationValue ("recipient") String        recipient,
            @DestinationValue ("id")        String        id) {
        //System.out.println (String.format ("wisper `%s -> %s: %s`", author, recipient, body));
        //System.out.println (String.format ("%s > %s %s (#%s)", author, recipient, body, id));
        final Optional <Boolean> spy = configStorage.get ("spy", Boolean::parseBoolean);
        final String login = configStorage.get ("login").get ();
        if (login.equals (author)) { // private message from local user
            MessageEntity message = new MessageEntity (recipient, id, time, 
                                             author, login, body, PRIVATE);
            messageHistory.addMessage (message);
        } else if (login.equals (recipient)) { // private message to local user
            MessageEntity message = new MessageEntity (author, id, time, 
                                      author, recipient, body, PRIVATE);            
            messageHistory.addMessage (message);
        } else if (spy.isPresent () && spy.get ()) {
            MessageEntity message = new MessageEntity ("spy", id, time, 
                                     author, recipient, body, PRIVATE);            
            messageHistory.addMessage (message);            
        }
    }
    
    @MessageRouteDestination (namespace = "neerc\\..+", room = "", body = ".+\\((\\w|-)+\\).+")
    public void controllTaskChangeMessage (
            @DestinationValue ("body")   String        body,
            @DestinationValue ("time")   LocalDateTime time,
            @DestinationValue ("author") String        author,
            @DestinationValue ("id")     String        id) {
        //System.out.println (String.format ("public `%s: %s`", author, body));
        customIQProvider.query ("tasks");
    }
    
    @MessageRouteDestination (namespace = "neerc\\..+", room = "", body = ".+clock.+")
    public void controllClockSynchronization (
            @DestinationValue ("body")    String        body,
            @DestinationValue ("time")    LocalDateTime time,
            @DestinationValue ("author")  String        author,
            @DestinationValue ("id")      String        id,
            @DestinationValue ("message") Object        message) {
        //System.out.println (String.format ("public `%s: %s`", author, body));
        System.out.println (message + " " + body);
    }
    
    @MessageRouteDestination (namespace = "conference\\..+", room = "neerc", roomExpectation = false)
    public void controllOtherRoomChatMessage (
            @DestinationValue ("body")   String        body,
            @DestinationValue ("time")   LocalDateTime time,
            @DestinationValue ("author") String        author,
            @DestinationValue ("room")   String        room,
            @DestinationValue ("id")     String        id) {
        //System.out.println (String.format ("public `%s: %s`", author, body));
        MessageEntity message = new MessageEntity (room, id, time, author, room, body, ROOM_PRIVATE);
        messageHistory.addMessage (message);
    }
    
    @PresenceRouteDestination (namespace = "conference\\..+", room = ".*")
    public void controllPresence (
            @DestinationValue ("author")    String        author,
            @DestinationValue ("available") Boolean       isAvailable,
            @DestinationValue ("time")      LocalDateTime time) {
        final OnlineStatus status = isAvailable 
                                  ? OnlineStatus.ONLINE 
                                  : OnlineStatus.OFFLINE;
        usersService.changeUserPresence (author, status, time);
    }
    
    @IQRouteDestination (from = "\\w+@neerc\\..+", namespace="\\w+@conference\\..+?#users")
    public void controllIQListOfUsers (@DestinationValue ("iq") UsersListIQ iq) {
        usersService.mergeUsers (iq.getUsers ());
    }
    
    @IQRouteDestination (from = "\\w+@neerc\\..+", namespace="\\w+@conference\\..+?#tasks")
    public void controllIQListOfTasks (@DestinationValue ("iq") TasksListIQ iq) {
        tasksService.mergeTasks (iq.getTasks ());
    }
    
}
