package app.state;

import app.StageManagerGUI;
import controller.gui.ManageUsersControllerGUI;

public class ManageUsersState extends PrimaryState {

    public ManageUsersState(StateManager stateManager) {
        super(stateManager);
    }

    @Override
    protected void loadContent() {
        ManageUsersControllerGUI manageUsersController = 
            stateManager.getStageManager().<ManageUsersControllerGUI>loadContent(StageManagerGUI.MANAGE_USERS_VIEW);

        if (manageUsersController != null) {
            manageUsersController.setStateManager(stateManager);
			manageUsersController.loadUsers();
        }
    }
}