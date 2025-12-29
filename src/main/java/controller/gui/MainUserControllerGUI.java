package controller.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.util.Map;
import controller.app.LogoutController;
import app.state.BachecaState;
import app.state.CatalogoState;
import app.state.CercaState;
import app.state.InfoState;
import app.state.ProfiloState;
import app.state.WishlistState;
import app.state.MainGuestState;

public class MainUserControllerGUI extends AbstractMainControllerGUI {

    @FXML private Button catalogoButton, cercaButton, bachecaButton, profileButton, wishlistButton, infoButton, logoutButton;
    private LogoutController logoutController;

    @FXML
    public void initialize() {
        stateButtonMap = Map.of(
            CatalogoState.class, catalogoButton,
            CercaState.class, cercaButton,
            BachecaState.class, bachecaButton,
            ProfiloState.class, profileButton,
            WishlistState.class, wishlistButton,
            InfoState.class, infoButton
        );
        logoutController = new LogoutController();
    }

    @FXML
    private void showCatalogo() { stateManager.setState(new CatalogoState(stateManager)); }

    @FXML
    private void showCerca() { stateManager.setState(new CercaState(stateManager)); }

    @FXML
    private void showBacheca() { stateManager.setState(new BachecaState(stateManager)); }

    @FXML
    private void showProfile() { stateManager.setState(new ProfiloState(stateManager)); }
    
    @FXML
    private void showWishlist() { stateManager.setState(new WishlistState(stateManager)); }

    @FXML
    private void showInfo() { stateManager.setState(new InfoState(stateManager)); }

    @FXML
    private void handleLogout() {
        logoutController.logout(); // logica applicativa pura
        stateManager.setState(new MainGuestState(stateManager));
    }
}