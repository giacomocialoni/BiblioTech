package app;

import javafx.application.Application;
import javafx.stage.Stage;
import app.state.MainGuestState;
import app.state.StateManager;
import dao.factory.DAOFactory;

public class ApplicationGUI extends Application {
    
    private static DAOFactory daoFactory;
    
    public static void setDaoFactory(DAOFactory factory) {
        daoFactory = factory;
    }
    
    @Override
    public void start(Stage primaryStage) {
        if (daoFactory == null) {
            throw new IllegalStateException("DAOFactory non impostata. Chiama setDaoFactory() prima di launchApp().");
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