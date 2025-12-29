package app.state;

import app.StageManagerGUI;
import controller.gui.SignInControllerGUI;

public class SignInState extends AuthState {
    
    public SignInState(StateManager stateManager) {
        super(stateManager);
    }
    
    @Override
    public void onEnter() {
        SignInControllerGUI controller = stateManager.getStageManager()
            .<SignInControllerGUI>loadContent(StageManagerGUI.SIGN_IN_VIEW);
        if (controller != null) {
            controller.setStateManager(stateManager);
        }
    }
}