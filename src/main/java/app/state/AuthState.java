package app.state;

/**
 * Stato base per gli stati di autenticazione (Login, SignIn).
 * Non mostrano il menu principale e hanno un back button.
 */
public abstract class AuthState extends AbstractState {
    
    protected AuthState(StateManager stateManager) {
        super(stateManager, StateType.AUTH_FLOW);
    }
    
    @Override
    public boolean showsMain() {
        return false; // AuthState non mostra il menu
    }
    
    @Override
    public boolean hasBackButton() {
        return true; // AuthState ha sempre back button
    }
}