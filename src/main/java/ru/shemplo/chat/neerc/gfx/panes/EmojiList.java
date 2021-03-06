package ru.shemplo.chat.neerc.gfx.panes;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.shemplo.chat.neerc.gfx.WindowManager;
import ru.shemplo.chat.neerc.gfx.scenes.MainSceneHolder;
import ru.shemplo.chat.neerc.gfx.scenes.SceneHolder;

public class EmojiList extends AbsTabContent {

    protected ObservableList <String> emojiList;
    protected ListView <String> emojiListView;
    
    public EmojiList (SceneHolder holder, String dialog) {
        super (false, (MainSceneHolder) holder, dialog);
        
        emojiList = FXCollections.observableArrayList ();
        
        Platform.runLater (() -> {
            emojiList.add ("alarm-clock");
            emojiList.add ("alien");
            emojiList.add ("angel");
            emojiList.add ("anger");
            emojiList.add ("angry-1");
            emojiList.add ("angry-2");
            emojiList.add ("angry");
            emojiList.add ("archive");
            emojiList.add ("arms-up");
            emojiList.add ("arm-up");
            emojiList.add ("arrogant");
            emojiList.add ("baby-1");
            emojiList.add ("baby");
            emojiList.add ("bald");
            emojiList.add ("balloon");
            emojiList.add ("barber-shop");
            emojiList.add ("bar-chart-1");
            emojiList.add ("bar-chart");
            emojiList.add ("barrel");
            emojiList.add ("bathtub-1");
            emojiList.add ("bathtub");
            emojiList.add ("battery");
            emojiList.add ("beads");
            emojiList.add ("bed-1");
            emojiList.add ("bed");
            emojiList.add ("bell");
            emojiList.add ("bikini");
            emojiList.add ("bolt");
            emojiList.add ("bomb");
            emojiList.add ("book-1");
            emojiList.add ("book-2");
            emojiList.add ("book-3");
            emojiList.add ("books");
            emojiList.add ("book");
            emojiList.add ("boot");
            emojiList.add ("bored");
            emojiList.add ("bow");
            emojiList.add ("box-1");
            emojiList.add ("box");
            emojiList.add ("boy-1");
            emojiList.add ("boy");
            emojiList.add ("bride");
            emojiList.add ("briefcase");
            emojiList.add ("broken-heart");
            emojiList.add ("calendar-1");
            emojiList.add ("calendar-2");
            emojiList.add ("calendar-3");
            emojiList.add ("calendar-4");
            emojiList.add ("calendar");
            emojiList.add ("candle");
            emojiList.add ("cat-1");
            emojiList.add ("cat-2");
            emojiList.add ("cat-3");
            emojiList.add ("cat-4");
            emojiList.add ("cat-5");
            emojiList.add ("cat-6");
            emojiList.add ("cat-7");
            emojiList.add ("cat-8");
            emojiList.add ("cat");
            emojiList.add ("chain");
            emojiList.add ("chat");
            emojiList.add ("cigarette");
            emojiList.add ("clapping");
            emojiList.add ("clipboard");
            emojiList.add ("clock");
            emojiList.add ("clutch");
            emojiList.add ("coffin");
            emojiList.add ("compact-disc-1");
            emojiList.add ("compact-disc");
            emojiList.add ("confetti-1");
            emojiList.add ("confetti");
            emojiList.add ("confused");
            emojiList.add ("controller");
            emojiList.add ("cool-1");
            emojiList.add ("cool");
            emojiList.add ("couple-1");
            emojiList.add ("couple-2");
            emojiList.add ("couple-3");
            emojiList.add ("couple");
            emojiList.add ("credit-card");
            emojiList.add ("crown");
            emojiList.add ("crying-1");
            emojiList.add ("crying-2");
            emojiList.add ("crying");
            emojiList.add ("cupid");
            emojiList.add ("cute");
            emojiList.add ("dancers");
            emojiList.add ("dancer");
            emojiList.add ("devil-1");
            emojiList.add ("devil");
            emojiList.add ("diamond");
            emojiList.add ("dislike");
            emojiList.add ("dizzy-1");
            emojiList.add ("dizzy");
            emojiList.add ("doctor");
            emojiList.add ("door");
            emojiList.add ("dress");
            emojiList.add ("drops");
            emojiList.add ("drop");
            emojiList.add ("ear");
            emojiList.add ("email-1");
            emojiList.add ("email");
            emojiList.add ("embarrassed");
            emojiList.add ("emoji");
            emojiList.add ("engagement-ring");
            emojiList.add ("explosion");
            emojiList.add ("eyes");
            emojiList.add ("family");
            emojiList.add ("fart");
            emojiList.add ("faucet");
            emojiList.add ("fax");
            emojiList.add ("file-1");
            emojiList.add ("file-2");
            emojiList.add ("file-3");
            emojiList.add ("file");
            emojiList.add ("film");
            emojiList.add ("fire");
            emojiList.add ("fist");
            emojiList.add ("flashlight");
            emojiList.add ("flask");
            emojiList.add ("flirt");
            emojiList.add ("floppy-disk");
            emojiList.add ("folder-1");
            emojiList.add ("folder-2");
            emojiList.add ("folder");
            emojiList.add ("footprint");
            emojiList.add ("frame");
            emojiList.add ("gear");
            emojiList.add ("geisha");
            emojiList.add ("gift");
            emojiList.add ("girl");
            emojiList.add ("goofy");
            emojiList.add ("greed");
            emojiList.add ("grinning");
            emojiList.add ("guard");
            emojiList.add ("gun");
            emojiList.add ("hair-cut");
            emojiList.add ("hammers");
            emojiList.add ("hammer");
            emojiList.add ("hand-1");
            emojiList.add ("handbag");
            emojiList.add ("hand");
            emojiList.add ("happy-1");
            emojiList.add ("happy-2");
            emojiList.add ("happy-3");
            emojiList.add ("happy-4");
            emojiList.add ("happy-5");
            emojiList.add ("happy-6");
            emojiList.add ("happy-7");
            emojiList.add ("happy");
            emojiList.add ("hard-disk");
            emojiList.add ("hat");
            emojiList.add ("heart-1");
            emojiList.add ("heart-2");
            emojiList.add ("heart-3");
            emojiList.add ("heart-4");
            emojiList.add ("heart-5");
            emojiList.add ("heart-6");
            emojiList.add ("heart-7");
            emojiList.add ("heart-8");
            emojiList.add ("heart-9");
            emojiList.add ("heart");
            emojiList.add ("high-heel");
            emojiList.add ("highlighter");
            emojiList.add ("hole");
            emojiList.add ("horn");
            emojiList.add ("hourglass-1");
            emojiList.add ("hourglass");
            emojiList.add ("idea");
            emojiList.add ("inbox");
            emojiList.add ("injury");
            emojiList.add ("in-love");
            emojiList.add ("jeans");
            emojiList.add ("joystick");
            emojiList.add ("key-1");
            emojiList.add ("keyboard");
            emojiList.add ("key");
            emojiList.add ("kimono");
            emojiList.add ("kiss-1");
            emojiList.add ("kiss-2");
            emojiList.add ("kiss-3");
            emojiList.add ("kissing-1");
            emojiList.add ("kissing");
            emojiList.add ("kiss");
            emojiList.add ("knife");
            emojiList.add ("koinobori");
            emojiList.add ("lantern-1");
            emojiList.add ("lantern");
            emojiList.add ("laptop");
            emojiList.add ("laughing-1");
            emojiList.add ("laughing-2");
            emojiList.add ("laughing");
            emojiList.add ("letter-1");
            emojiList.add ("letter");
            emojiList.add ("libra");
            emojiList.add ("like");
            emojiList.add ("link");
            emojiList.add ("lipstick");
            emojiList.add ("loss");
            emojiList.add ("loupe");
            emojiList.add ("love-letter");
            emojiList.add ("magic-ball");
            emojiList.add ("mailbox-1");
            emojiList.add ("mailbox-2");
            emojiList.add ("mailbox-3");
            emojiList.add ("mail-box");
            emojiList.add ("mailbox");
            emojiList.add ("man-1");
            emojiList.add ("man-2");
            emojiList.add ("man");
            emojiList.add ("mask");
            emojiList.add ("massage");
            emojiList.add ("microphone");
            emojiList.add ("microscope");
            emojiList.add ("middle-finger");
            emojiList.add ("money-1");
            emojiList.add ("money-2");
            emojiList.add ("money-bag");
            emojiList.add ("money");
            emojiList.add ("monitor");
            emojiList.add ("monkey-1");
            emojiList.add ("monkey-2");
            emojiList.add ("monkey");
            emojiList.add ("monster");
            emojiList.add ("mouse");
            emojiList.add ("mouth");
            emojiList.add ("muted");
            emojiList.add ("nail-polish");
            emojiList.add ("negative");
            emojiList.add ("nerd");
            emojiList.add ("nervous-1");
            emojiList.add ("nervous");
            emojiList.add ("newspaper-1");
            emojiList.add ("newspaper");
            emojiList.add ("nose");
            emojiList.add ("notebook-1");
            emojiList.add ("notebook-2");
            emojiList.add ("notebook");
            emojiList.add ("ok");
            emojiList.add ("old-woman");
            emojiList.add ("open-book");
            emojiList.add ("outbox");
            emojiList.add ("package");
            emojiList.add ("padlock-1");
            emojiList.add ("padlock-2");
            emojiList.add ("padlock");
            emojiList.add ("pager");
            emojiList.add ("paint-brush");
            emojiList.add ("paperclip-1");
            emojiList.add ("paperclip");
            emojiList.add ("pen-1");
            emojiList.add ("pencil");
            emojiList.add ("pen");
            emojiList.add ("photo-camera-1");
            emojiList.add ("photo-camera");
            emojiList.add ("pick");
            emojiList.add ("pill");
            emojiList.add ("pin");
            emojiList.add ("please");
            emojiList.add ("plug");
            emojiList.add ("pointing-left");
            emojiList.add ("pointing-right");
            emojiList.add ("pointing-up");
            emojiList.add ("policeman");
            emojiList.add ("polo-shirt");
            emojiList.add ("poo");
            emojiList.add ("pound-sterling");
            emojiList.add ("princess");
            emojiList.add ("printer");
            emojiList.add ("punch");
            emojiList.add ("purse");
            emojiList.add ("push-pin");
            emojiList.add ("radio");
            emojiList.add ("raise-hand");
            emojiList.add ("remote-control-1");
            emojiList.add ("remote-control");
            emojiList.add ("repair-tools");
            emojiList.add ("ribbon");
            emojiList.add ("ruler");
            emojiList.add ("run");
            emojiList.add ("sad-10");
            emojiList.add ("sad-11");
            emojiList.add ("sad-12");
            emojiList.add ("sad-13");
            emojiList.add ("sad-1");
            emojiList.add ("sad-2");
            emojiList.add ("sad-3");
            emojiList.add ("sad-4");
            emojiList.add ("sad-5");
            emojiList.add ("sad-6");
            emojiList.add ("sad-7");
            emojiList.add ("sad-8");
            emojiList.add ("sad-9");
            emojiList.add ("sad");
            emojiList.add ("salute");
            emojiList.add ("sandal");
            emojiList.add ("satellite-dish");
            emojiList.add ("scare");
            emojiList.add ("scissors");
            emojiList.add ("scream");
            emojiList.add ("scroll");
            emojiList.add ("serious");
            emojiList.add ("set-square");
            emojiList.add ("shield");
            emojiList.add ("shining");
            emojiList.add ("shirt-1");
            emojiList.add ("shirt-2");
            emojiList.add ("shirt");
            emojiList.add ("shocked");
            emojiList.add ("shoe");
            emojiList.add ("shooting-star");
            emojiList.add ("shopping-bag");
            emojiList.add ("shopping-cart");
            emojiList.add ("shower");
            emojiList.add ("shy");
            emojiList.add ("sick");
            emojiList.add ("skull");
            emojiList.add ("sleep-1");
            emojiList.add ("sleep");
            emojiList.add ("sleepy");
            emojiList.add ("smartphone-1");
            emojiList.add ("smartphone");
            emojiList.add ("smart");
            emojiList.add ("smile-1");
            emojiList.add ("smile");
            emojiList.add ("sneakers");
            emojiList.add ("sofa");
            emojiList.add ("star");
            emojiList.add ("stopwatch-1");
            emojiList.add ("stopwatch");
            emojiList.add ("straight-1");
            emojiList.add ("straight");
            emojiList.add ("strong");
            emojiList.add ("sunglasses");
            emojiList.add ("surprised-1");
            emojiList.add ("surprised-2");
            emojiList.add ("surprised-3");
            emojiList.add ("surprised-4");
            emojiList.add ("surprised");
            emojiList.add ("surprise");
            emojiList.add ("suspicious");
            emojiList.add ("swords");
            emojiList.add ("sword");
            emojiList.add ("syringe");
            emojiList.add ("tag-1");
            emojiList.add ("tag");
            emojiList.add ("tap-1");
            emojiList.add ("tap");
            emojiList.add ("telephone-1");
            emojiList.add ("telephone");
            emojiList.add ("telescope");
            emojiList.add ("television");
            emojiList.add ("tengu");
            emojiList.add ("thermometer");
            emojiList.add ("thinking");
            emojiList.add ("toilet");
            emojiList.add ("tongue-out-1");
            emojiList.add ("tongue-out-2");
            emojiList.add ("tongue-out");
            emojiList.add ("tongue");
            emojiList.add ("top-hat");
            emojiList.add ("trash");
            emojiList.add ("umbrella");
            emojiList.add ("unlocked");
            emojiList.add ("users");
            emojiList.add ("user");
            emojiList.add ("vain");
            emojiList.add ("vase-1");
            emojiList.add ("vase");
            emojiList.add ("vhs");
            emojiList.add ("victory");
            emojiList.add ("video-camera-1");
            emojiList.add ("video-camera-2");
            emojiList.add ("video-camera");
            emojiList.add ("vise");
            emojiList.add ("walk");
            emojiList.add ("watch");
            emojiList.add ("waving-hand");
            emojiList.add ("wink-1");
            emojiList.add ("wink");
            emojiList.add ("woman-1");
            emojiList.add ("woman-2");
            emojiList.add ("woman");
            emojiList.add ("wrench");
            emojiList.add ("yen");
            emojiList.add ("zzz");
        });
        
        final VBox content = new VBox (8.0);
        
        final Label info = new Label ("To copy emoji code to clipboard click on it with secondary mouse button");
        VBox.setMargin (info, new Insets (0, 0, 0, 32.0));
        content.getChildren ().add (info);
        
        emojiListView = new ListView <> ();
        emojiListView.setCellFactory (__ -> new EmojiCell ());
        emojiListView.setBackground (Background.EMPTY);
        VBox.setVgrow (emojiListView, Priority.ALWAYS);
        emojiListView.editableProperty ().set (false);
        emojiListView.setItems (emojiList);
        
        content.getChildren ().add (emojiListView);
        setContent (content);
    }

    @Override
    public void onResponsibleTabOpened (Tab owner) {}

    @Override
    public void onResponsibleTabClosed (Tab owner) {}
    
    private static final class EmojiCell extends ListCell <String> {
        
        private static WindowManager manager;
        
        static {
            try {
                manager = WindowManager.getInstance ();
            } catch (InterruptedException e) {/* impossible */}
        }
        
        public EmojiCell () {
            setBackground (Background.EMPTY);
            
            setOnContextMenuRequested (cme -> {
                final Clipboard cb = Clipboard.getSystemClipboard ();
                final ClipboardContent cc = new ClipboardContent ();
                cc.putString (String.format (":%s:", getItem ()));
                cb.setContent (cc);
            });
        }
        
        @Override
        protected void updateItem (String item, boolean empty) {
            super.updateItem (item, empty || item == null);
            if (empty || item == null) { 
                setGraphic (null);
                return; 
            }
            
            final MessageInterpreter messageInterpreter = manager
                . getSharedContext ().getMessageInterpreter ();
            
            final HBox line = new HBox (8.0);
            line.maxWidthProperty ().bind (getListView ().widthProperty ().subtract (16.0));
            line.getStyleClass ().add ("message-row");
            line.setPadding (new Insets (4));
            line.setMinHeight (30.0);
            
            final Label emojiIcon = messageInterpreter.makeIconLabel (item, 42);
            emojiIcon.getStyleClass ().add ("message-source");
            emojiIcon.setAlignment  (Pos.CENTER);
            
            final Label emojiCode = new Label (String.format (":%s:", item));
            emojiCode.getStyleClass ().add ("message-source");
            emojiCode.setAlignment  (Pos.CENTER);
            
            final Label emojiName = new Label (item);
            emojiName.getStyleClass ().add ("message-source");
            emojiName.setAlignment  (Pos.CENTER);
            
            line.getChildren ().addAll (emojiIcon, emojiCode, emojiName);
            setWrapText (true);
            setGraphic (line);
        }
        
    }
    
}
