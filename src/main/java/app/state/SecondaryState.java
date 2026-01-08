package app.state;

/**
 * Stato base per tutti gli stati secondari (dettagli libro, conferme, ecc).
 * Non mostrano il menu principale e hanno un back button.
 */
public abstract class SecondaryState extends AbstractState {
    
    protected SecondaryState(StateManager stateManager) {
        super(stateManager, StateType.SECONDARY);
    }
    
    @Override
    public boolean showsMain() {
        return false; // SecondaryState non mostra il menu
    }
    
    @Override
    public boolean hasBackButton() {
        return true; // SecondaryState ha sempre back button
    }
}