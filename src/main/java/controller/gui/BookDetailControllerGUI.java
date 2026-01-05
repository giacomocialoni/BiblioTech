package controller.gui;

import app.Session;
import app.state.ConfirmLoanState;
import app.state.ConfirmPurchaseState;
import app.state.ErrorState;
import app.state.LoginState;
import app.state.StateManager;
import bean.BookBean;
import controller.app.BookDetailController;
import controller.app.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import view.components.BookDetailFactory;

public class BookDetailControllerGUI {

	private static final String IN_WISHLIST = "in-wishlist";
	
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label genreLabel;
    @FXML private Label yearLabel;
    @FXML private Label publisherLabel;
    @FXML private Label pagesLabel;
    @FXML private Label isbnLabel;
    @FXML private Label availabilityLabel;
    @FXML private Label plotLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private ImageView coverImage;
    @FXML private Button backButton;
    @FXML private Button buyButton;
    @FXML private Button borrowButton;
    @FXML private Button wishlistButton;

    private StateManager stateManager;
    private BookDetailController appController;
    private LoginController loginController;
    private BookBean currentBook;

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        this.appController = new BookDetailController();
        this.loginController = new LoginController();
    }

    public void loadBook(int bookId) {
        updateBackButtonText();

        BookBean book = appController.getBookById(bookId);
        this.currentBook = book;
        
        if (book == null) {
        	stateManager.setState(new ErrorState(
                    stateManager,
                    "Libro non trovato! Il libro potrebbe essere stato rimosso."
                ));
        	return;
        }

        updateQuantitySpinner(book.getStock());
        BookDetailFactory.populateBookDetails(this, book);
        
        double total = book.getPrice() * quantitySpinner.getValue();
        buyButton.setText(String.format("Acquista - €%.2f", total));
        
        if (Session.getInstance().isLoggedIn() && appController.isInWishlist(bookId)) {
            wishlistButton.setText("Rimuovi");
            wishlistButton.getStyleClass().add(IN_WISHLIST);
        } else {
            wishlistButton.setText("Aggiungi");
            wishlistButton.getStyleClass().remove(IN_WISHLIST);
        }
    }

    @FXML
    public void initialize() {
        // Spinner quantità
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantitySpinner.setValueFactory(valueFactory);
        quantitySpinner.setEditable(true);
        
        quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (currentBook != null) {
                double total = currentBook.getPrice() * newValue;
                buyButton.setText(String.format("Acquista - €%.2f", total));
            }
        });
    }
    
    @FXML
    public void handleWishlist() {
        if (currentBook == null) return;

        if (!Session.getInstance().isLoggedIn()) {
            stateManager.setState(
                new LoginState(stateManager, () -> toggleWishlist())
            );
            return;
        }

        toggleWishlist();
    }

    //TODO Sposta exception nel controller applicativo
    private void toggleWishlist() {
        boolean inWishlist = appController.isInWishlist(currentBook.getId());
        boolean success;

        if (inWishlist) {
            success = appController.removeFromWishlist(currentBook.getId());
            if (success) {
                wishlistButton.setText("Aggiungi");
                wishlistButton.getStyleClass().remove(IN_WISHLIST);
            }
        } else {
            success = appController.addToWishlist(currentBook.getId());
            if (success) {
                wishlistButton.setText("Rimuovi");
                if (!wishlistButton.getStyleClass().contains(IN_WISHLIST)) {
                    wishlistButton.getStyleClass().add(IN_WISHLIST);
                }
            }
        }

        if (!success) {
            stateManager.setState(new ErrorState(
                stateManager,
                "Errore durante l'aggiornamento della wishlist"
            ));
        }
    }

    private void updateQuantitySpinner(int stock) {
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Math.max(1, stock), 1);
        quantitySpinner.setValueFactory(valueFactory);
    }

    @FXML
    public void handleBuy() {
        if (!loginController.isLoggedIn()) {
            stateManager.setState(new LoginState(stateManager, () -> proceedToBuy()));
        } else {
            proceedToBuy();
        }
    }
    private void proceedToBuy() {
        if (currentBook != null) {
            int quantity = quantitySpinner.getValue();
            stateManager.setState(new ConfirmPurchaseState(stateManager, currentBook, quantity));
        }
    }
    
    @FXML
    public void handleBorrow() {
        // NUOVO: controlla se l'utente è loggato
        if (!Session.getInstance().isLoggedIn()) {
            // Vai allo stato di login con callback per il prestito
            stateManager.setState(new LoginState(stateManager, () -> {
                // Callback chiamata dopo il login riuscito
                if (currentBook != null) {
                    stateManager.setState(new ConfirmLoanState(
                        stateManager, 
                        currentBook
                    ));
                }
            }));
        } else {
            // Utente già loggato, procedi normalmente
            if (currentBook != null) {
                stateManager.setState(new ConfirmLoanState(
                    stateManager, 
                    currentBook
                ));
            }
        }
    }

    @FXML
    public void handleBack() {
        stateManager.goBack();
    }

    private void updateBackButtonText() {
        backButton.setText("← Torna indietro"); // Testo generico
    }

    // Getters
    public Label getTitleLabel() { return titleLabel; }
    public Label getAuthorLabel() { return authorLabel; }
    public Label getGenreLabel() { return genreLabel; }
    public Label getYearLabel() { return yearLabel; }
    public Label getPublisherLabel() { return publisherLabel; }
    public Label getPagesLabel() { return pagesLabel; }
    public Label getIsbnLabel() { return isbnLabel; }
    public Label getAvailabilityLabel() { return availabilityLabel; }
    public Label getPlotLabel() { return plotLabel; }
    public ImageView getCoverImage() { return coverImage; }
    public Spinner<Integer> getQuantitySpinner() { return quantitySpinner; }
    public Button getBuyButton() { return buyButton; }
    public Button getBorrowButton() { return borrowButton; }
}