package controller.gui;

import app.Session;
import app.state.StateManager;
import controller.app.ProfiloController;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import view.components.BookCardFactory;
import view.components.LoanBookCardFactory;

import bean.BookBean;
import bean.LoanBean;
import bean.UserBean;

import java.util.List;

public class ProfiloControllerGUI {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private Button showPasswordButton;
    @FXML private HBox recentPurchasesBox;
    @FXML private HBox loansBox;
    @FXML private ScrollPane loansPane;
    @FXML private ScrollPane purchasesPane;
    @FXML private Label LoansLabel;
    @FXML private Label PurchasesLabel;

    private final ProfiloController appController = new ProfiloController();
    private BookCardFactory bookCardFactory;
    private boolean passwordVisible = false;
    private UserBean user;

    private Image visibleIcon;
    private Image invisibleIcon;
    private ImageView eyeView;

    @FXML
    private void initialize() {
        visibleIcon = new Image(getClass().getResource("/images/utils/view.png").toExternalForm());
        invisibleIcon = new Image(getClass().getResource("/images/utils/hide.png").toExternalForm());

        eyeView = new ImageView(invisibleIcon);
        eyeView.setFitWidth(18);
        eyeView.setFitHeight(18);
        showPasswordButton.setGraphic(eyeView);
        showPasswordButton.setStyle("-fx-background-color: transparent;");

        showPasswordButton.setOnMouseEntered(e -> showPasswordButton.setCursor(Cursor.HAND));
        showPasswordButton.setOnMouseExited(e -> showPasswordButton.setCursor(Cursor.DEFAULT));
        showPasswordButton.setOnAction(e -> togglePasswordVisibility());
    }

    public void setStateManager(StateManager stateManager) {
        if (!Session.getInstance().isLoggedIn()) {
            // Se non loggato, vai al login
            stateManager.setState(new app.state.LoginState(stateManager));
            return;
        }

        String email = Session.getInstance().getLoggedUser().getEmail();
        this.user = appController.getUser(email);
        if (user == null) return;

        this.bookCardFactory = new BookCardFactory(stateManager);

        populateProfile();
        populateRecentPurchases();
        populateLoans();
    }

    private void populateProfile() {
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        updatePasswordField();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        updatePasswordField();
        eyeView.setImage(passwordVisible ? visibleIcon : invisibleIcon);
    }

    private void updatePasswordField() {
        passwordField.setText(passwordVisible ? user.getPassword() : "••••••••");
    }

    private void populateRecentPurchases() {
        recentPurchasesBox.getChildren().clear();
        List<BookBean> books = appController.getPurchasedBooks(user.getEmail());

        if (books.isEmpty()) {
            PurchasesLabel.setText("Non hai ancora acquistato libri");
            purchasesPane.setVisible(false);
            purchasesPane.setManaged(false);
            return;
        }

        purchasesPane.setVisible(true);
        purchasesPane.setManaged(true);

        for (BookBean book : books) {
            StackPane card = bookCardFactory.createBookCard(book);
            recentPurchasesBox.getChildren().add(card);
        }
    }

    private void populateLoans() {
        loansBox.getChildren().clear();
        List<LoanBean> loans = appController.getActiveLoans(user.getEmail());

        if (loans.isEmpty()) {
            LoansLabel.setText("Non hai prestiti attivi");
            loansPane.setVisible(false);
            loansPane.setManaged(false);
            return;
        }

        loansPane.setVisible(true);
        loansPane.setManaged(true);

        LoanBookCardFactory loanCardFactory =
                new LoanBookCardFactory(bookCardFactory);

        for (LoanBean loan : loans) {
            VBox card = loanCardFactory.createLoanCard(loan);
            loansBox.getChildren().add(card);
        }
    }
    
}