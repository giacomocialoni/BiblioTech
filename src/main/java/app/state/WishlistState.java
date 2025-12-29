package app.state;

import app.StageManagerGUI;
import controller.gui.WishlistControllerGUI;

public class WishlistState extends PrimaryState {
    
    public WishlistState(StateManager stateManager) {
        super(stateManager);
    }

    @Override
    protected void loadContent() {
        WishlistControllerGUI controllerWishlist =
            stateManager.getStageManager().<WishlistControllerGUI>loadContent(StageManagerGUI.WISHLIST_VIEW);

        if (controllerWishlist != null) {
            controllerWishlist.setStateManager(stateManager);
        }
    }
}