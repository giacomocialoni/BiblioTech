package controller.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.Map;

import app.state.BachecaState;
import app.state.CatalogoState;
import app.state.CercaState;
import app.state.InfoState;
import app.state.LoginState;

public class MainGuestControllerGUI extends AbstractMainControllerGUI {

    @FXML private Button catalogoButton, cercaButton, bachecaButton, infoButton, loginButton;

    @FXML
    public void initialize() {
        stateButtonMap = Map.of(
            CatalogoState.class, catalogoButton,
            CercaState.class, cercaButton,
            BachecaState.class, bachecaButton,
            InfoState.class, infoButton,
            LoginState.class, loginButton
        );
    }

    @FXML
    private void showCatalogo() {
        stateManager.setState(new CatalogoState(stateManager));
    }

    @FXML
    private void showCerca() {
        stateManager.setState(new CercaState(stateManager));
    }

    @FXML
    private void showBacheca() {
        stateManager.setState(new BachecaState(stateManager));
    }

    @FXML
    private void showInfo() {
        stateManager.setState(new InfoState(stateManager));
    }

    @FXML
    private void showLogin() {
        stateManager.setState(new LoginState(stateManager));
    }
}