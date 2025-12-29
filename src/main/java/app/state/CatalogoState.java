package app.state;

import app.StageManagerGUI;
import controller.gui.CatalogoControllerGUI;

public class CatalogoState extends PrimaryState {
    
    public CatalogoState(StateManager stateManager) {
        super(stateManager);
    }
    
    @Override
    protected void loadContent() {
        CatalogoControllerGUI controllerCatalogo =
            stateManager.getStageManager().<CatalogoControllerGUI>loadContent(StageManagerGUI.CATALOGO_VIEW);

        if (controllerCatalogo != null) {
            controllerCatalogo.setStateManager(stateManager);
        }
    }
}