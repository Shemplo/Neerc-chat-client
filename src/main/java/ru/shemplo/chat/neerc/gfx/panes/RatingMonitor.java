package ru.shemplo.chat.neerc.gfx.panes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class RatingMonitor extends AbsTabContent {
    
    public RatingMonitor (String tabName) {
        super (false, tabName);
        
        
        HBox settingsBar = new HBox (8.0);
        settingsBar.setAlignment (Pos.CENTER_LEFT);
        settingsBar.setPadding (new Insets (8.0));
        getChildren ().add (settingsBar);
        
        Label sourceLabel = new Label ("Source:");
        settingsBar.getChildren ().add (sourceLabel);
        
        TextField source = new TextField ();
        settingsBar.getChildren ().add (source);
    }

    @Override
    public void onResponsibleTabOpened (Tab owner) {
        
    }
    
}
