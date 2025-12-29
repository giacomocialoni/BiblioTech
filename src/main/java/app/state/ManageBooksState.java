package app.state;

import app.StageManagerGUI;
import controller.gui.ManageBooksControllerGUI;

public class ManageBooksState extends PrimaryState {
    
    public ManageBooksState(StateManager stateManager) {
        super(stateManager);
    }
    
    @Override
    protected void loadContent() {
        ManageBooksControllerGUI manageBookController =
                stateManager.getStageManager().<ManageBooksControllerGUI>loadContent(StageManagerGUI.MANAGE_BOOKS_VIEW);

        if (manageBookController != null) {
            manageBookController.setStateManager(stateManager);
            manageBookController.loadBooks();
        }
    }
}