package controller.gui;

import app.state.StateManager;
import app.state.CreateBookState;
import app.state.ErrorState;
import app.state.SuccessState;
import bean.BookBean;
import controller.app.ManageBooksController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import view.components.ManageBooksCardFactory;

import java.util.List;

public class ManageBooksControllerGUI {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Button addBookButton;
    @FXML private Label resultsLabel;
    @FXML private FlowPane resultsContainer;

    private StateManager stateManager;
    private final ManageBooksController appController = new ManageBooksController();
    private ManageBooksCardFactory cardFactory;
    
    private boolean initialized = false;
    private static final String SUCCESS = "Successo", ERROR = "Errore";

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        this.cardFactory = new ManageBooksCardFactory();
        
        if (initialized) {
            loadBooks();
        }
    }

    @FXML
    public void initialize() {
        initialized = true;
    }

    @FXML
    public void handleSearch() {
        if (cardFactory == null) {
            showError("Attenzione", "Sistema non ancora inizializzato");
            return;
        }

        String searchText = searchField.getText().trim();
        List<BookBean> books;

        if (searchText.isEmpty()) {
            books = appController.getAllBooks();
        } else {
            books = appController.searchBooks(searchText);
        }

        displayBooks(books);
    }

    @FXML
    public void handleClearFilters() {
        searchField.clear();
        loadBooks();
    }

    @FXML
    public void addBook() {
        CreateBookState createBookState = new CreateBookState(stateManager);
        stateManager.setState(createBookState);
    }

    public void loadBooks() {
        if (cardFactory == null) return;

        List<BookBean> books = appController.getAllBooks();
        displayBooks(books);
    }

    private void displayBooks(List<BookBean> books) {
        resultsContainer.getChildren().clear();

        for (BookBean book : books) {
            var bookCard = cardFactory.createBookCard(
                book,
                (quantity) -> handleIncreaseStock(book.getId(), quantity),
                (quantity) -> handleDecreaseStock(book.getId(), quantity),
                () -> handleRemoveBook(book.getId())
            );
            resultsContainer.getChildren().add(bookCard);
        }

        updateResultsLabel(books.size());
    }

    private void updateResultsLabel(int count) {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            resultsLabel.setText("Totale libri: " + count);
        } else {
            resultsLabel.setText("Trovati " + count + " libri per '" + searchText + "'");
        }
    }

    // ===== GESTIONE AZIONI =====

    private void handleIncreaseStock(int bookId, int quantity) {
        try {
            appController.increaseStock(bookId, quantity);
            loadBooks(); // Ricarica la lista
            showSuccess(SUCCESS, "Stock aumentato di " + quantity + " libri");
        } catch (Exception e) {
            showError(ERROR, "Errore nell'aumentare lo stock: " + e.getMessage());
        }
    }

    private void handleDecreaseStock(int bookId, int quantity) {
        try {
        	BookBean book = appController.getBookById(bookId);
            if (book != null && book.getStock() >= quantity) {
                appController.decreaseStock(bookId, quantity);
                loadBooks(); // Ricarica la lista
                showSuccess(SUCCESS, "Stock diminuito di " + quantity + " libri");
            } else {
                showError(ERROR, "Stock insufficiente");
            }
        } catch (Exception e) {
            showError(ERROR, "Errore nel diminuire lo stock: " + e.getMessage());
        }
    }

    private void handleRemoveBook(int bookId) {
        try {
            // Conferma prima di eliminare
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Conferma eliminazione");
            alert.setHeaderText("Eliminare il libro?");
            alert.setContentText("Questa operazione non può essere annullata.");
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                appController.deleteBook(bookId);
                loadBooks(); // Ricarica la lista
                showSuccess(SUCCESS, "Libro eliminato con successo");
            }
        } catch (Exception e) {
            showError(ERROR, "Errore nell'eliminare il libro: " + e.getMessage());
        }
    }

    // ===== GESTIONE STATI =====

    private void showSuccess(String title, String message) {
        SuccessState successState = new SuccessState(stateManager, message);
        stateManager.setState(successState);
    }

    private void showError(String title, String message) {
        ErrorState errorState = new ErrorState(stateManager, message);
        stateManager.setState(errorState);
    }
}