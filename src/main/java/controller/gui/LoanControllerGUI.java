package controller.gui;

import app.state.ErrorState;
import app.state.SuccessState;
import bean.BookBean;
import app.state.StateManager;
import controller.app.facade.UserLoanFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import utils.LoanResult;

public class LoanControllerGUI {

    @FXML private Label titleLabel;
    @FXML private Label bookTitleLabel;
    @FXML private Label durationLabel;
    @FXML private Label priceLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private StateManager stateManager;
    private BookBean book;
    private UserLoanFacade userLoanFacade;

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        this.userLoanFacade = new UserLoanFacade();
    }

    public void setBorrowData(BookBean book) {
        this.book = book;
        updateUI();
    }

    private void updateUI() {
        titleLabel.setText("Conferma Prestito");
        bookTitleLabel.setText(book.getTitle());
        durationLabel.setText("30 giorni");
        priceLabel.setText("Gratis");
        
        confirmButton.setDisable(book.getStock() <= 0);
        
        if (book.getStock() <= 0) {
            confirmButton.setDisable(true);
            confirmButton.setText("Non disponibile");
        } else {
            confirmButton.setText("Conferma Prestito");
        }
    }

    @FXML
    private void handleCancel() {
        stateManager.goBack();
    }

    @FXML
    private void handleConfirm() {
        LoanResult result = userLoanFacade.loanBook(book.getId());
        
        switch (result) {
            case SUCCESS -> {
                stateManager.setState(new SuccessState(
                        stateManager, 
                        "Prestito effettuato con successo! Ricorda di restituire entro 30 giorni."
                    ));
            }
            case INSUFFICIENT_STOCK -> {
                stateManager.setState(new ErrorState(
                    stateManager, 
                    "Libro non più disponibile per il prestito!"
                ));
            }
            case MAX_LOANS_REACHED -> {
                stateManager.setState(new ErrorState(
                    stateManager, 
                    "Hai raggiunto il limite massimo di 3 prestiti attivi.\nRestituisci un libro per prenderne un altro."
                ));
            }
            case EXPIRED_LOAN_EXISTS -> {
                stateManager.setState(new ErrorState(
                    stateManager, 
                    "Hai prestiti scaduti da restituire prima di prenderne di nuovi."
                ));
            }
            case NOT_LOGGED -> {
                stateManager.setState(new ErrorState(
                    stateManager,
                    "Devi essere loggato per effettuare un prestito."
                ));
            }
            case ERROR -> {
                stateManager.setState(new ErrorState(
                    stateManager, 
                    "Errore durante il prestito. Riprova più tardi."
                ));
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + result);
        }
    }
}