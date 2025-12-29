package app.state;

import app.StageManagerGUI;
import controller.gui.SuccessControllerGUI;

public class SuccessState extends SecondaryState {

    private final String successMessage;

    public SuccessState(StateManager stateManager, String successMessage) {
        super(stateManager);
        this.successMessage = successMessage;
    }

    @Override
    public void onEnter() {
        SuccessControllerGUI controller = stateManager.getStageManager()
            .<SuccessControllerGUI>loadContent(StageManagerGUI.SUCCESS_VIEW);
        if (controller != null) {
            controller.setStateManager(stateManager);
            controller.setSuccessData(successMessage);
        }
    }
}