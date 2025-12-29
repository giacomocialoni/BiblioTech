package app.state;

import app.StageManagerGUI;
import controller.gui.BachecaControllerGUI;

public class BachecaState extends PrimaryState {

    public BachecaState(StateManager stateManager) {
        super(stateManager);
    }

    @Override
    protected void loadContent() {
        BachecaControllerGUI controllerBacheca =
            stateManager.getStageManager().<BachecaControllerGUI>loadContent(StageManagerGUI.BACHECA_VIEW);

        if (controllerBacheca != null) {
            controllerBacheca.setStateManager(stateManager);
        }
    }
}