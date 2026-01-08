package controller.gui;

import java.util.Map;
import controller.app.LogoutController;
import app.state.ManageBooksState;
import app.state.ManageUsersState;
import app.state.PostState;
import app.state.ReservationState;
import app.state.ReturnLoanState;
import app.state.MainGuestState;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainAdminControllerGUI extends AbstractMainControllerGUI {

	@FXML
	private Button prenotazioniButton;
	@FXML
	private Button gestioneLibriButton;
	@FXML
	private Button gestioneUtentiButton;
	@FXML
	private Button postButton;
	@FXML
	private Button logoutButton;
	@FXML
	private Button prestitiButton;
	
    private LogoutController logoutController;

    @FXML
    public void initialize() {
        stateButtonMap = Map.of(
            ReservationState.class, prenotazioniButton,
            ReturnLoanState.class, prestitiButton,
            ManageBooksState.class, gestioneLibriButton,
            ManageUsersState.class, gestioneUtentiButton,
            PostState.class, postButton
        );
        logoutController = new LogoutController();
    }

    @FXML
    private void showReservations() { stateManager.setState(new ReservationState(stateManager)); }
    
    @FXML
    private void showReturnLoans() { stateManager.setState(new ReturnLoanState(stateManager)); }

    @FXML
    private void showManageBooks() { stateManager.setState(new ManageBooksState(stateManager)); }

    @FXML
    private void showManageUsers() { stateManager.setState(new ManageUsersState(stateManager)); }

    @FXML
    private void showPost() { stateManager.setState(new PostState(stateManager)); }

    @FXML
    private void handleLogout() {
        logoutController.logout(); // logica applicativa pura
        stateManager.setState(new MainGuestState(stateManager));
    }
}