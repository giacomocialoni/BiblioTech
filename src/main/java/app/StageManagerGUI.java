package app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import controller.gui.MainGuestControllerGUI;
import controller.gui.MainUserControllerGUI;
import controller.gui.MainAdminControllerGUI;
import controller.gui.MainControllerGUI;

import dao.factory.DAOFactory;
import app.state.AppState;
import app.state.StateChangeListener;
import app.state.StateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageManagerGUI implements StateChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(StageManagerGUI.class);
    
    private final StateManager stateManager;
    private final Stage primaryStage;
    
    private MainGuestControllerGUI mainGuestController;
    private MainUserControllerGUI mainUserController;
    private MainAdminControllerGUI mainAdminController;
    
    private static final String BASE_PATH = "/view/gui/";
    
    // Main Views
    public static final String MAIN_GUEST_VIEW = BASE_PATH + "MainGuestView.fxml";
    public static final String MAIN_USER_VIEW = BASE_PATH + "MainUserView.fxml";
    public static final String MAIN_ADMIN_VIEW = BASE_PATH + "MainAdminView.fxml";
    
    // Primary State Views
    public static final String CATALOGO_VIEW = BASE_PATH + "CatalogoView.fxml";
    public static final String CERCA_VIEW = BASE_PATH + "CercaView.fxml";
    public static final String BACHECA_VIEW = BASE_PATH + "BachecaView.fxml";
    public static final String INFO_VIEW = BASE_PATH + "InfoView.fxml";
    public static final String PROFILO_VIEW = BASE_PATH + "ProfiloView.fxml";
    public static final String WISHLIST_VIEW = BASE_PATH + "WishlistView.fxml";
    public static final String RESERVATION_VIEW = BASE_PATH + "ReservationView.fxml";
    public static final String MANAGE_BOOKS_VIEW = BASE_PATH + "ManageBooksView.fxml";
    public static final String MANAGE_USERS_VIEW = BASE_PATH + "ManageUsersView.fxml";
    public static final String POST_VIEW = BASE_PATH + "PostView.fxml";
    public static final String RETURN_LOANS_VIEW = BASE_PATH + "ReturnLoansView.fxml";
    
    // Secondary State Views
    public static final String BOOK_DETAIL_VIEW = BASE_PATH + "BookDetailView.fxml";
    public static final String CONFIRM_LOAN_VIEW = BASE_PATH + "LoanView.fxml";
    public static final String CONFIRM_PURCHASE_VIEW = BASE_PATH + "PurchaseView.fxml";
    public static final String CREATE_BOOK_VIEW = BASE_PATH + "CreateBookView.fxml";
    public static final String ERROR_VIEW = BASE_PATH + "ErrorView.fxml";
    public static final String SUCCESS_VIEW = BASE_PATH + "SuccessView.fxml";
    
    // Auth State Views
    public static final String LOGIN_VIEW = BASE_PATH + "LoginView.fxml";
    public static final String SIGN_IN_VIEW = BASE_PATH + "SignInView.fxml";
    
    public StageManagerGUI(Stage stage, DAOFactory daoFactory) {
        this.primaryStage = stage;
        this.stateManager = new StateManager(daoFactory, this);
        this.stateManager.setStateChangeListener(this);
    }
    
    @Override
    public void onStateChanged(AppState state) {
        // Aggiorna la visibilità del menu principale in base allo stato
        MainControllerGUI main = getActiveMainController();
        if (main != null) {
            main.setVisible(state.showsMain());
        }
    }
    
    /**
     * Mostra/nascondi il menu principale.
     * Usato dagli stati per controllare la visibilità.
     */
    public void showMainView(boolean show) {
        MainControllerGUI main = getActiveMainController();
        if (main != null) {
            main.setVisible(show);
        }
    }
    
    public void loadMainGuestView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_GUEST_VIEW));
            Parent root = loader.load();
            
            mainGuestController = loader.getController();
            mainGuestController.setStateManager(stateManager);
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("BiblioTech - Guest");
            primaryStage.sizeToScene();
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();
            
            // Reset altri controller
            mainUserController = null;
            mainAdminController = null;
            
        } catch (Exception e) {
            logger.error("Errore nel caricamento della view guest: {}", MAIN_GUEST_VIEW, e);
        }
    }
    
    public MainUserControllerGUI loadMainUserView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_USER_VIEW));
            Parent root = loader.load();
            
            MainUserControllerGUI controller = loader.getController();
            controller.setStateManager(stateManager);
            mainUserController = controller;
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("BiblioTech - User");
            primaryStage.sizeToScene();
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();
            
            // Reset altri controller
            mainGuestController = null;
            mainAdminController = null;
            
            return controller;
        } catch (Exception e) {
            logger.error("Errore nel caricamento della view user: {}", MAIN_USER_VIEW, e);
            return null;
        }
    }
    
    public MainAdminControllerGUI loadMainAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_ADMIN_VIEW));
            Parent root = loader.load();
            
            MainAdminControllerGUI controller = loader.getController();
            controller.setStateManager(stateManager);
            mainAdminController = controller;
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("BiblioTech - Admin");
            primaryStage.sizeToScene();
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();
            
            // Reset altri controller
            mainGuestController = null;
            mainUserController = null;
            
            return controller;
        } catch (Exception e) {
            logger.error("Errore nel caricamento della view admin: {}", MAIN_ADMIN_VIEW, e);
            return null;
        }
    }
    
    public MainControllerGUI getActiveMainController() {
        if (mainUserController != null) return mainUserController;
        if (mainAdminController != null) return mainAdminController;
        return mainGuestController;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                logger.error("File FXML non trovato: {}", fxmlPath);
                return null;
            }
            
            Node node = loader.load();
            
            MainControllerGUI activeController = getActiveMainController();
            if (activeController != null) {
                activeController.setContent(node);
            } else {
                logger.warn("Nessun controller principale attivo per caricare il contenuto: {}", fxmlPath);
            }
            
            return (T) loader.getController();
        } catch (Exception e) {
            logger.error("Errore durante il caricamento del contenuto FXML: {}", fxmlPath, e);
            return null;
        }
    }
    
    public StateManager getStateManager() {
        return stateManager;
    }
}