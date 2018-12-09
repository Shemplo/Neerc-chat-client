package ru.shemplo.chat.neerc.gfx.scenes;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import ru.shemplo.chat.neerc.enities.UserEntity;
import ru.shemplo.chat.neerc.enities.UserEntity.OnlineStatus;
import ru.shemplo.chat.neerc.enities.UserEntity.UserPower;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.gfx.panes.Conversation;
import ru.shemplo.chat.neerc.gfx.panes.TaskTile;
import ru.shemplo.chat.neerc.network.TasksService;
import ru.shemplo.chat.neerc.network.UsersService;
import ru.shemplo.chat.neerc.network.listeners.TasksStatusListener;
import ru.shemplo.chat.neerc.network.listeners.UserPresenceListener;
import ru.shemplo.snowball.stuctures.Pair;

@RequiredArgsConstructor
public class MainSceneListener implements UserPresenceListener, TasksStatusListener {
    
    private final MainSceneHolder holder;
    
    {
        
    }

    @Override
    public void onUserChangedPresence (String user, OnlineStatus status) {
        Image image = Stream.of (status)
                . map (OnlineStatus::name)
                . map (String::toLowerCase)
                . map (s -> String.format ("/gfx/user-%s.png", s))
                . map (Image::new)
                . findFirst ()
                . get ();
        final ImageView view = new ImageView (image);
        SceneComponent.USERS.<HBox> safe (holder.getScene ())
                      .ifPresent (line -> {
            Platform.runLater (() -> {
                line.getChildren ().stream ()
                    .map       (o -> (Button) o)
                    .filter    (b -> b.getText ().equals (user))
                    .findFirst ()
                    .ifPresent (b -> b.setGraphic (view));
            });
        });
    }
    
    private static final Comparator <UserEntity> 
        UE_COMPARATOR = (a, b) -> {
            if (Objects.equals (a.getPower (), b.getPower ())) {
                if (Objects.equals (a.getGroup (), b.getGroup ())) {
                    return a.getName ().compareTo (b.getName ());
                }
                
                return a.getGroup ().compareTo (b.getGroup ());
            }
            
            return UserPower.POWER.equals (a.getPower ()) ? -1 : 1;
        };

    @Override
    public void onUsersUpdated () {
        SceneComponent.USERS.<HBox> safe (holder.getScene ())
                      .ifPresent (line -> {
            Platform.runLater (line.getChildren ()::clear);
            
            final UsersService usersService = holder.getManager ().getUsersService ();
            List <Button> buttons = usersService.getUsers ().stream ()
            . sorted (UE_COMPARATOR)
            . map (u -> Pair.mp (u, u.getPower ().name ().toLowerCase ()))
            . map (p -> p.applyS (n -> String.format ("status-%s", n)))
            . map (p -> {
                Image status = Stream.of (p.F.getStatus ())
                             . map (OnlineStatus::name)
                             . map (String::toLowerCase)
                             . map (s -> String.format ("/gfx/user-%s.png", s))
                             . map (Image::new).findFirst ().get ();
                final ImageView view = new ImageView (status);
                Button button = new Button (p.F.getName (), view);
                button.setOnMouseClicked (this::onUserButtonClick);
                button.getStyleClass ().add ("metal-button");
                button.getStyleClass ().add (p.S);
                button.setGraphicTextGap (8.0);
                return button;
            })
            . collect (Collectors.toList ());
            Platform.runLater (() -> line.getChildren ().addAll (buttons));
        });
    }
    
    private void onUserButtonClick (MouseEvent me) {
        Button source = (Button) me.getSource ();
        final String title = source.getText ();
        Conversation conversation = holder.getOrCreateAndGetConversation (title);
        Tab tab = holder.getOrCreateAndGetTabFor (title, conversation);
        
        TabPane conversations = SceneComponent.CONVERSATIONS
                              . get (holder.getScene ());
        int index = conversations.getTabs ().indexOf (tab);
        if (index != -1) {
            conversations.getSelectionModel ().select (index);
        }
    }
    
    private final ConcurrentMap <String, TaskTile> 
    tasks = new ConcurrentHashMap <> (); 

    @Override
    public synchronized void onTasksUpdated () {
        SceneComponent.TASKS.<VBox> safe (holder.getScene ()).ifPresent (line -> {
            Platform.runLater (line.getChildren ()::clear);
            
            final WindowManager manager = holder.getManager ();
            final TasksService tasksService = manager.getTasksService ();
            final String user = manager.getConfigStorage ().get ("login")
                              . orElse ("[user name]");
            List <TaskTile> tiles = tasksService.getActualTasksFor (user).stream ()
                                  . sorted  ((a, b) -> a.getTime ().compareTo (b.getTime ()))
                                  . map     (task -> Pair.mp (task, new TaskTile (holder, task)))
                                  . peek    (p -> tasks.put (p.F.getId (), p.S))
                                  . map     (p -> p.S)
                                  . collect (Collectors.toList ());
            Platform.runLater (() -> line.getChildren ().addAll (tiles));
        });
    }
    
    @Override
    public void onTaskUpdated (String id) {
        holder.getManager ().getTasksService ().getTaskByID (id)
              .ifPresent (task -> {
            TaskTile tile = tasks.get (task.getId ());
            if (tile == null) { return; }
            
            final String user = holder.getManager ().getConfigStorage ()
                              . get ("login").orElse ("[user name]");
            task.getStatusFor (user).ifPresent (status -> {                
                tile.changeStatusTo (status.F, status.S);
            });
        });
    }
    
}
