package ru.shemplo.chat.neerc.gfx.panes;

import static ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess.*;

import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.shemplo.chat.neerc.enities.MessageEntity.MessageAccess;

@RequiredArgsConstructor
public abstract class AbsTabContent extends VBox {
    
    @Getter protected final boolean sendingMessageEnable;
    @Getter protected MessageAccess access = PUBLIC;
    @Getter @Setter protected String input = "";
    @Getter protected final String dialog;
    
    public abstract void onResponsibleTabOpened (Tab owner);
    
}
