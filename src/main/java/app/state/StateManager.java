package app.state;

import java.util.ArrayDeque;
import java.util.Deque;
import dao.factory.DAOFactory;
import app.StageManagerGUI;

public class StateManager {
    private final DAOFactory daoFactory;
    private final StageManagerGUI stageManager;
    private AppState currentState;
    private final Deque<AppState> history = new ArrayDeque<>();
    private StateChangeListener listener;
    
    public StateManager(DAOFactory daoFactory, StageManagerGUI stageManager) {
        this.daoFactory = daoFactory;
        this.stageManager = stageManager;
    }
    
    public StageManagerGUI getStageManager() {
        return stageManager;
    }
    
    public void setStateChangeListener(StateChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Imposta un nuovo stato attivo.
     * Gestisce automaticamente la history e la visibilità del menu.
     */
    public void setState(AppState newState) {
        // Se stiamo passando a un MainState, pulisci la history
        if (newState.getType() == StateType.MAIN) {
            history.clear();
        }
        
        // Salva lo stato corrente nella history se necessario
        if (currentState != null && shouldSaveToHistory(currentState)) {
            history.push(currentState);
        }
        
        // Esegui transizione di stato
        if (currentState != null) {
            currentState.onExit();
        }
        
        currentState = newState;
        currentState.onEnter();
        
        // Notifica il cambio di stato
        notifyStateChanged();
    }
    
    /**
     * Gestisce il comportamento "back" in base al tipo di stato corrente.
     */
    public void goBack() {
        if (history.isEmpty()) return;
        
        switch (currentState.getType()) {
            case AUTH_FLOW:
                // Auth: torna semplicemente allo stato precedente
                goBackOneStep();
                break;
                
            case SECONDARY:
                // Secondary: torna al Detail più recente
                goBackToDetail();
                break;
                
            case DETAIL:
                // Detail: torna al Primary più recente
                goBackToPrimary();
                break;
                
            default:
                // Main e Primary: non hanno back button
                break;
        }
    }
    
    /**
     * Torna indietro di un solo passo nella history.
     */
    private void goBackOneStep() {
        if (history.isEmpty()) return;
        switchToState(history.pop());
    }
    
    /**
     * Per DetailState: torna al PrimaryState più recente.
     */
    private void goBackToPrimary() {
        // Trova il primo PrimaryState nella history
        AppState targetPrimary = findFirstStateOfType(StateType.PRIMARY);
        
        // Se trovato un primary, vai ad esso
        if (targetPrimary != null) {
            switchToState(targetPrimary);
        } else if (!history.isEmpty()) {
            // Se non ci sono primary, torna semplicemente indietro
            goBackOneStep();
        }
    }
    
    /**
     * Per SecondaryState: torna al DetailState più recente.
     */
    private void goBackToDetail() {
        // Trova il primo DetailState nella history
        AppState targetDetail = findFirstStateOfType(StateType.DETAIL);
        
        if (targetDetail != null) {
            switchToState(targetDetail);
        } else {
            // Se non ci sono Detail, prova a trovare un Primary
            goBackToPrimary();
        }
    }
    
    /**
     * Trova il primo stato di un tipo specifico nella history.
     * Rimuove tutti gli stati sopra di esso.
     */
    private AppState findFirstStateOfType(StateType targetType) {
        Deque<AppState> tempStack = new ArrayDeque<>();
        AppState targetState = null;
        
        // Cerca il primo stato del tipo target
        while (!history.isEmpty()) {
            AppState candidate = history.pop();
            if (candidate.getType() == targetType) {
                targetState = candidate;
                break;
            }
            tempStack.push(candidate);
        }
        
        // Ripristina gli stati sopra al target
        while (!tempStack.isEmpty()) {
            history.push(tempStack.pop());
        }
        
        return targetState;
    }
    
    /**
     * Passa a uno stato specifico.
     */
    private void switchToState(AppState state) {
        if (currentState != null) {
            currentState.onExit();
        }
        
        currentState = state;
        currentState.onEnter();
        notifyStateChanged();
    }
    
    /**
     * Determina se uno stato deve essere salvato nella history.
     * MainState non viene mai salvato.
     */
    private boolean shouldSaveToHistory(AppState state) {
        return state.getType() != StateType.MAIN;
    }
    
    private void notifyStateChanged() {
        if (listener != null) {
            listener.onStateChanged(currentState);
        }
    }
    
    public DAOFactory getDaoFactory() {
        return daoFactory;
    }
    
    public Deque<AppState> getHistory() {
        return new ArrayDeque<>(history); // Copia difensiva
    }
    
    public AppState getCurrentState() {
        return currentState;
    }
}