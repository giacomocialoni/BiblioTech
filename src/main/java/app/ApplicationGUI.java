package app;

import javafx.application.Application;
import javafx.stage.Stage;
import app.state.MainGuestState;
import app.state.StateManager;
import dao.factory.DAOFactory;

public class ApplicationGUI extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Recupero la factory direttamente dal Singleton
        DAOFactory daoFactory = DAOFactory.getInstance();
        if (daoFactory == null) {
            throw new IllegalStateException("DAOFactory non inizializzata. Chiama DAOFactory.init() prima di launchApp().");
        }
        
        StageManagerGUI stageManager = new StageManagerGUI(primaryStage, daoFactory);
        StateManager stateManager = stageManager.getStateManager();
        
        // All'avvio carichiamo il MainGuestState (che caricherà automaticamente il Catalogo)
        stateManager.setState(new MainGuestState(stateManager));
    }
    
    public static void launchApp(String[] args) {
        launch(args);
    }
}