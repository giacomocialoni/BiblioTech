package controller.gui;

import app.state.ErrorState;
import app.state.StateManager;
import app.state.SuccessState;
import bean.BookBean;
import controller.app.facade.UserPurchaseFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import utils.BuyResult;

public class PurchaseControllerGUI {

    @FXML private Label titleLabel;
    @FXML private Label bookTitleLabel;
    @FXML private Label quantityLabel;
    @FXML private Label priceLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private StateManager stateManager;
    private BookBean book;
    private int quantity;
    private UserPurchaseFacade purchaseFacade;

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        this.purchaseFacade = new UserPurchaseFacade();
    }

    public void setPurchaseData(BookBean book, int quantity) {
        this.book = book;
        this.quantity = quantity;
        updateUI();
    }

    private void updateUI() {
        // Popola l'interfaccia con i dati dell'acquisto
        titleLabel.setText("Conferma Acquisto");
        bookTitleLabel.setText(book.getTitle());
        quantityLabel.setText(quantity + " copie");
        
        // Prezzo unitario
        priceLabel.setText(String.format("%.2f €", book.getPrice()));
        
        // Calcola il totale
        double total = book.getPrice() * quantity;
        
        // Disabilita il pulsante se stock insufficiente O utente non può comprare
        boolean canPurchase = purchaseFacade.canPurchase();
        boolean enoughStock = book.getStock() >= quantity;
        
        confirmButton.setDisable(!canPurchase || !enoughStock);
        
        if (!canPurchase) {
            confirmButton.setText("Accedi per acquistare");
            confirmButton.setStyle("-fx-background-color: #ff9800;");
        } else if (!enoughStock) {
            confirmButton.setText("Stock insufficiente");
            confirmButton.setStyle("-fx-background-color: #f44336;");
        } else {
            confirmButton.setText(String.format("Conferma (€%.2f)", total));
            confirmButton.setStyle("-fx-background-color: #4CAF50;");
        }
    }

    @FXML
    private void handleCancel() {
        stateManager.goBack();
    }

    @FXML
    private void handleConfirm() {
        BuyResult result = purchaseFacade.buyBook(book.getId(), quantity);

        switch (result) {
            case SUCCESS -> stateManager.setState(
                new SuccessState(stateManager, "Acquisto effettuato con successo!")
            );
            case INSUFFICIENT_STOCK -> stateManager.setState(
                new ErrorState(stateManager, "Stock insufficiente! Sono disponibili solo " + book.getStock() + " copie.")
            );
            case NOT_LOGGED -> stateManager.setState(
                new ErrorState(stateManager, "Devi essere loggato per effettuare acquisti.")
            );
            case UNAUTHORIZED -> stateManager.setState(
                new ErrorState(stateManager, "Il tuo account non permette acquisti.")
            );
            case ERROR -> stateManager.setState(
                new ErrorState(stateManager, "Errore durante l'acquisto. Riprova più tardi.")
            );
            default -> throw new IllegalArgumentException("Unexpected value: " + result);
        }
    }
}