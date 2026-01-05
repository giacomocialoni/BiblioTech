package controller.gui;

import app.state.StateManager;
import app.state.ErrorState;
import app.state.SuccessState;
import controller.app.facade.AdminLoanFacade;
import controller.app.facade.AdminPurchaseFacade;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import bean.LoanBean;
import bean.PurchaseBean;
import view.components.ReservationCardFactory;

import java.util.List;

public class ReservationControllerGUI {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private VBox resultsContainer;
    @FXML private Label resultsLabel;
    @FXML private Label searchLabel;

    @FXML private CheckBox showSalesCheckbox;
    @FXML private CheckBox showLoansCheckbox;

    @FXML private Button userFilterButton;
    @FXML private Button bookFilterButton;

    private StateManager stateManager;
    private final AdminPurchaseFacade adminPurchaseFacade = new AdminPurchaseFacade();
    private final AdminLoanFacade adminLoanFacade = new AdminLoanFacade();
    private ReservationCardFactory cardFactory;
    
    private static final String SUCCESS = "Successo", ERROR = "Errore", ACTIVE = "active";
    private boolean initialized = false;
    private String searchMode = "user";

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        this.cardFactory = new ReservationCardFactory();
        
        if (initialized) {
            loadAllReservations();
        }
    }

    @FXML
    public void initialize() {
        showSalesCheckbox.setSelected(true);
        showLoansCheckbox.setSelected(true);
        setUserFilter();
        initialized = true;
        
        // Carica le prenotazioni all'inizializzazione
        loadAllReservations();
    }

    @FXML
    public void setUserFilter() {
        searchMode = "user";
        updateFilterButtons();
        searchLabel.setText("Cerca utente:");
        searchField.setPromptText("Inserisci email, nome o cognome utente");
    }

    @FXML
    public void setBookFilter() {
        searchMode = "book";
        updateFilterButtons();
        searchLabel.setText("Cerca libro:");
        searchField.setPromptText("Inserisci titolo o autore del libro");
    }

    private void updateFilterButtons() {
        userFilterButton.getStyleClass().remove(ACTIVE);
        bookFilterButton.getStyleClass().remove(ACTIVE);
        
        if ("user".equals(searchMode)) {
            if (!userFilterButton.getStyleClass().contains(ACTIVE)) {
                userFilterButton.getStyleClass().add(ACTIVE);
            }
        } else {
            if (!bookFilterButton.getStyleClass().contains(ACTIVE)) {
                bookFilterButton.getStyleClass().add(ACTIVE);
            }
        }
    }

    @FXML
    public void handleSearch() {
        if (cardFactory == null) {
            showError("Attenzione", "Sistema non ancora inizializzato");
            return;
        }

        String searchText = searchField.getText().trim();
        boolean includeSales = showSalesCheckbox.isSelected();
        boolean includeLoans = showLoansCheckbox.isSelected();

        if (searchText.isEmpty()) {
            loadAllReservations();
            return;
        }

        List<PurchaseBean> purchases = List.of();
        List<LoanBean> loans = List.of();

        if (includeSales) {
            purchases = "user".equals(searchMode)
                ? adminPurchaseFacade.searchPurchasesByUser(searchText)
                : adminPurchaseFacade.searchPurchasesByBook(searchText);
        }

        if (includeLoans) {
            loans = "user".equals(searchMode)
                ? adminLoanFacade.searchLoansByUser(searchText)
                : adminLoanFacade.searchLoansByBook(searchText);
        }

        displayReservations(purchases, loans);
    }

    @FXML
    public void handleClearFilters() {
        searchField.clear();
        showSalesCheckbox.setSelected(true);
        showLoansCheckbox.setSelected(true);
        setUserFilter();
        loadAllReservations();
    }

    @FXML
    public void handleCheckboxChange() {
        if (cardFactory != null) {
            if (searchField.getText().trim().isEmpty()) {
                loadAllReservations();
            } else {
                handleSearch();
            }
        }
    }

    private void loadAllReservations() {
        try {
            if (cardFactory == null) return;

            boolean includeSales = showSalesCheckbox.isSelected();
            boolean includeLoans = showLoansCheckbox.isSelected();

            List<PurchaseBean> purchases =
                includeSales ? adminPurchaseFacade.getAllReservedPurchases() : List.of();

            List<LoanBean> loans =
                includeLoans ? adminLoanFacade.getAllReservedLoans() : List.of();

            displayReservations(purchases, loans);

        } catch (Exception e) {
            showError(ERROR, "Errore nel caricamento delle prenotazioni");
        }
    }

    private void displayReservations(
            List<PurchaseBean> purchases,
            List<LoanBean> loans
    ) {
        try {
            resultsContainer.getChildren().clear();
            int count = 0;

            // Processa acquisti
            for (PurchaseBean purchase : purchases) {
                if (purchase.getBook() != null) {
                    resultsContainer.getChildren().add(
                        cardFactory.createPurchaseCard(
                            purchase,
                            purchase.getBook(),
                            () -> handleAcceptPurchase(purchase.getId(), purchase.getBookId()),
                            () -> handleRejectPurchase(purchase.getId())
                        )
                    );
                    count++;
                }
            }

            // Processa prestiti
            for (LoanBean loan : loans) {
                if (loan.getBook() != null) {
                    // NOTA: I LoanBean dovrebbero avere getBookId() per ottenere l'ID del libro
                    // Se non c'è, usa un metodo alternativo
                    int bookId = getBookIdFromLoan(loan);
                    resultsContainer.getChildren().add(
                        cardFactory.createLoanCard(
                            loan,
                            loan.getBook(),
                            () -> handleAcceptLoan(loan.getId(), bookId),
                            () -> handleRejectLoan(loan.getId())
                        )
                    );
                    count++;
                }
            }

            updateResultsLabel(count, purchases.size(), loans.size());

        } catch (Exception e) {
            showError(ERROR, "Errore nella visualizzazione dei risultati");
        }
    }
    
    private int getBookIdFromLoan(LoanBean loan) {
        // Metodo helper per ottenere l'ID del libro dal prestito
        // Controlla se il LoanBean ha getBookId() o se deve essere estratto dal BookBean
        if (loan.getBook() != null) {
            return loan.getBook().getId();
        }
        return -1; // Valore di default
    }

    private void updateResultsLabel(int total, int salesCount, int loansCount) {
        String modeText = "user".equals(searchMode) ? "per utente" : "per libro";
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            resultsLabel.setText("Prenotazioni trovate: " + total + " (Vendite: " + salesCount + ", Prestiti: " + loansCount + ")");
        } else {
            resultsLabel.setText("Trovate " + total + " prenotazioni " + modeText + " '" + searchText + "' (Vendite: " + salesCount + ", Prestiti: " + loansCount + ")");
        }
    }

    private void handleAcceptPurchase(int purchaseId, int bookId) {
        try {
            boolean success = adminPurchaseFacade.acceptPurchase(purchaseId);
            if (success) {
                loadAllReservations();
                showSuccess(SUCCESS, "Vendita accettata con successo!");
            } else {
                showError(ERROR, "Impossibile accettare la vendita");
            }
        } catch (Exception e) {
            showError(ERROR, "Errore nell'accettare la vendita: " + e.getMessage());
        }
    }

    private void handleRejectPurchase(int purchaseId) {
        try {
            boolean success = adminPurchaseFacade.rejectPurchase(purchaseId);
            if (success) {
                loadAllReservations();
                showSuccess(SUCCESS, "Vendita rifiutata!");
            } else {
                showError(ERROR, "Impossibile rifiutare la vendita");
            }
        } catch (Exception e) {
            showError(ERROR, "Errore nel rifiutare la vendita: " + e.getMessage());
        }
    }

    private void handleAcceptLoan(int loanId, int bookId) {
        try {
            boolean success = adminLoanFacade.acceptLoan(loanId);
            if (success) {
                loadAllReservations();
                showSuccess(SUCCESS, "Prestito accettato con successo!");
            } else {
                showError(ERROR, "Impossibile accettare il prestito");
            }
        } catch (Exception e) {
            showError(ERROR, "Errore nell'accettare il prestito: " + e.getMessage());
        }
    }

    private void handleRejectLoan(int loanId) {
        try {
            boolean success = adminLoanFacade.rejectLoan(loanId);
            if (success) {
                loadAllReservations();
                showSuccess(SUCCESS, "Prestito rifiutato!");
            } else {
                showError(ERROR, "Impossibile rifiutare il prestito");
            }
        } catch (Exception e) {
            showError(ERROR, "Errore nel rifiutare il prestito: " + e.getMessage());
        }
    }

    private void showSuccess(String title, String message) {
        stateManager.setState(new SuccessState(stateManager, message));
    }

    private void showError(String title, String message) {
        stateManager.setState(new ErrorState(stateManager, message));
    }
}