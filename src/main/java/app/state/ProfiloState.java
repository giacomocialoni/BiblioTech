package app.state;

import app.StageManagerGUI;
import controller.gui.ProfiloControllerGUI;

public class ProfiloState extends PrimaryState {

    public ProfiloState(StateManager stateManager) {
        super(stateManager);
    }

    @Override
    protected void loadContent() {
        ProfiloControllerGUI controllerProfilo =
            stateManager.getStageManager().<ProfiloControllerGUI>loadContent(StageManagerGUI.PROFILO_VIEW);

        if (controllerProfilo != null) {
            controllerProfilo.setStateManager(stateManager);
        }
    }
}