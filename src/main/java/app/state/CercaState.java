package app.state;

import app.StageManagerGUI;
import controller.gui.CercaControllerGUI;

public class CercaState extends PrimaryState {

    public CercaState(StateManager stateManager) {
        super(stateManager);
    }

    @Override
    protected void loadContent() {
        CercaControllerGUI controllerCerca =
            stateManager.getStageManager().<CercaControllerGUI>loadContent(StageManagerGUI.CERCA_VIEW);

        if (controllerCerca != null) {
            controllerCerca.setStateManager(stateManager);
        }
    }
}