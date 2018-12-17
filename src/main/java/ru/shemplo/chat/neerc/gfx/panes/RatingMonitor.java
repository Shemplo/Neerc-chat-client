package ru.shemplo.chat.neerc.gfx.panes;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import ru.shemplo.chat.neerc.gfx.scenes.MainSceneHolder;
import ru.shemplo.chat.neerc.gfx.scenes.SceneHolder;

public class RatingMonitor extends AbsTabContent {
    
    public RatingMonitor (SceneHolder holder, String tabName) {
        super (false, (MainSceneHolder) holder, tabName);
        
        Label sourceLabel = new Label ("Source:");
        toolbar.getChildren ().add (sourceLabel);
        
        TextField source = new TextField ();
        HBox.setHgrow (source, Priority.ALWAYS);
        toolbar.getChildren ().add (source);
        
        CheckBox refresh = new CheckBox ("auto refresh");
        toolbar.getChildren ().add (refresh);
    }

    @Override
    public void onResponsibleTabOpened (Tab owner) {
        
    }
    
    @Override
    public void onResponsibleTabClosed (Tab owner) {
        
    }
    
}
