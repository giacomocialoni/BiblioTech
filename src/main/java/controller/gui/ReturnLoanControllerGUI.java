package controller.gui;

import app.state.StateManager;
import app.state.ErrorState;
import app.state.SuccessState;
import bean.BookBean;
import bean.LoanBean;
import controller.app.ReturnLoanController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import view.components.ReturningLoanCardFactory;

import java.util.List;

public class ReturnLoanControllerGUI {

    @FXML private VBox resultsContainer;
    @FXML private Label resultsLabel;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Label searchLabel;
    @FXML private Button userFilterButton;
    @FXML private Button bookFilterButton;

    private StateManager stateManager;
    private final ReturnLoanController appController = new ReturnLoanController();
    private ReturningLoanCardFactory cardFactory;

    private boolean initialized = false;
    private String searchMode = "user";
    private static final String ACTIVE = "active";

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        this.cardFactory = new ReturningLoanCardFactory();
        if (initialized) {
            loadAllLoans();
        }
    }

    @FXML
    public void initialize() {
        setUserFilter();
        initialized = true;
    }

    // ===================== FILTER BUTTONS =====================
    @FXML
    public void setUserFilter() {
        searchMode = "user";
        updateFilterButtons();
        searchLabel.setText("Cerca utente:");
        searchField.setPromptText("Inserisci email/nome/cognome utente");
    }

    @FXML
    public void setBookFilter() {
        searchMode = "book";
        updateFilterButtons();
        searchLabel.setText("Cerca libro:");
        searchField.setPromptText("Inserisci titolo o autore libro");
    }

    private void updateFilterButtons() {
        userFilterButton.getStyleClass().remove(ACTIVE);
        bookFilterButton.getStyleClass().remove(ACTIVE);
        if ("user".equals(searchMode)) {
            userFilterButton.getStyleClass().add(ACTIVE);
        } else {
            bookFilterButton.getStyleClass().add(ACTIVE);
        }
    }

    // ===================== LOAD =====================
    private void loadAllLoans() {
        resultsContainer.getChildren().clear();
        List<LoanBean> loans = appController.getAllLoanedLoans();
        populateLoans(loans);
    }

    private void populateLoans(List<LoanBean> loans) {
        int count = 0;
        for (LoanBean loan : loans) {
            BookBean book = loan.getBook();
            if (book != null) {
                resultsContainer.getChildren().add(
                    cardFactory.createLoanCard(
                        loan,
                        book,
                        () -> handleReturnLoan(loan.getId())
                    )
                );
                count++;
            }
        }
        resultsLabel.setText("Prestiti in corso: " + count);
    }

    // ===================== ACTIONS =====================
    private void handleReturnLoan(int loanId) {
        if (appController.returnLoan(loanId)) {
            loadAllLoans();
            showSuccess("Successo", "Prestito restituito con successo!");
        } else {
            showError("Errore", "Errore nella restituzione del prestito");
        }
    }

    @FXML
    public void handleSearch() {
        resultsContainer.getChildren().clear();
        List<LoanBean> loans;
        String searchText = searchField.getText().trim();

        if ("user".equals(searchMode)) {
            loans = appController.searchLoanedLoansByUser(searchText);
        } else {
            loans = appController.searchLoanedLoansByBook(searchText);
        }
        populateLoans(loans);
    }

    @FXML
    public void handleClearFilters() {
        searchField.clear();
        setUserFilter();
        loadAllLoans();
    }

    @FXML
    public void handleCheckboxChange() {
        loadAllLoans();
    }

    // ===================== UI HELPERS =====================
    private void showSuccess(String title, String message) {
        SuccessState successState = new SuccessState(stateManager, message);
        stateManager.setState(successState);
    }

    private void showError(String title, String message) {
        ErrorState errorState = new ErrorState(stateManager, message);
        stateManager.setState(errorState);
    }
}