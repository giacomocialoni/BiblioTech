package app.state;

/**
 * Stato base per tutti gli stati primari (Catalogo, Bacheca, Cerca, Info).
 * Vengono visualizzati dentro al MainState e mostrano il menu.
 */
public abstract class PrimaryState extends AbstractState {
    
    protected PrimaryState(StateManager stateManager) {
        super(stateManager, StateType.PRIMARY);
    }
    
    @Override
    public void onEnter() {
        // Carica il contenuto FXML specifico
        loadContent();
        
        // Aggiorna il bottone attivo nel menu principale
        controller.gui.MainControllerGUI mainController = 
            stateManager.getStageManager().getActiveMainController();
        if (mainController != null) {
            mainController.updateActiveButtonByState();
        }
    }
    
    @Override
    public boolean showsMain() {
        return true; // PrimaryState mostra sempre il menu
    }
    
    @Override
    public boolean hasBackButton() {
        return false; // PrimaryState non ha back button
    }
    
    /** 
     * Metodo astratto per caricare il contenuto FXML specifico 
     */
    protected abstract void loadContent();
}