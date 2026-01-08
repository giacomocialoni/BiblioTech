package app.state;

/**
 * Stato base per tutti i MainStates (Guest, User, Admin).
 * Si occupa di caricare la vista principale e il suo default primary.
 */
public abstract class MainState extends AbstractState {
    
    protected MainState(StateManager stateManager) {
        super(stateManager, StateType.MAIN);
    }
    
    @Override
    public void onEnter() {
        // 1. Carica la vista principale appropriata
        loadMainView();
        
        // 2. Imposta il controller principale
        controller.gui.MainControllerGUI mainController = 
            stateManager.getStageManager().getActiveMainController();
        if (mainController != null) {
            mainController.setStateManager(stateManager);
        }
        
        // 3. Carica il primary predefinito
        AppState defaultPrimary = getDefaultPrimaryState();
        if (defaultPrimary != null) {
            stateManager.setState(defaultPrimary);
        }
    }
    
    /** 
     * Metodo astratto: ogni MainState specifica quale vista caricare 
     */
    protected abstract void loadMainView();
    
    /** 
     * Metodo astratto: ogni MainState specifica il suo stato primario predefinito 
     */
    protected abstract AppState getDefaultPrimaryState();
    
    @Override
    public boolean showsMain() {
        return true; // MainState mostra sempre il menu
    }
}