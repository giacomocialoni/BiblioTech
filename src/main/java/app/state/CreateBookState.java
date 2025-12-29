package app.state;

import app.StageManagerGUI;
import controller.gui.CreateBookControllerGUI;

public class CreateBookState extends SecondaryState {
    
    public CreateBookState(StateManager stateManager) {
        super(stateManager);
    }
    
    @Override
    public void onEnter() {
        CreateBookControllerGUI controller = stateManager.getStageManager()
            .<CreateBookControllerGUI>loadContent(StageManagerGUI.CREATE_BOOK_VIEW);
        if (controller != null) {
            controller.setStateManager(stateManager);
        }
    }
}