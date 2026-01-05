package controller.gui;

import app.state.*;
import bean.AccountBean;
import controller.app.SignInController;
import exception.EmailAlreadyRegisteredException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SignInControllerGUI {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Button backButton;
    @FXML private VBox loginContainer;
    @FXML private Label errorLabel;
    private FadeTransition errorFade;

    private StateManager stateManager;
    private SignInController signInController;

    private boolean passwordFieldTouched = false;

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        this.signInController = new SignInController();
    }

    @FXML
    private void initialize() {
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                passwordFieldTouched = true;
                validatePasswordLength();
            }
        });

        repeatPasswordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (passwordFieldTouched) validatePasswordMatch();
                else validatePasswordLength();
            }
        });
    }

    @FXML
    private void handleSignIn() {
        try {
            passwordFieldTouched = true;

            if (!validateAllFields()) return;

            AccountBean accountBean = signInController.signIn(
                    emailField.getText().trim(),
                    passwordField.getText(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim()
            );

            if (accountBean != null) {
                // SUCCESSO - vai alla main view
                stateManager.setState(new MainUserState(stateManager));
            }

        } catch (IllegalArgumentException e) {
            // Validazione input fallita
            showError("Tutti i campi sono obbligatori!");
            
        } catch (EmailAlreadyRegisteredException e) {
            handleDuplicateEmail(e);
            
        } catch (Exception e) {
            // Errore generico
            showError("Errore: " + e.getMessage());
        }
    }
    
    private void handleDuplicateEmail(EmailAlreadyRegisteredException e) {
        // 1. Messaggio di errore nella label rossa
    	showError(e.getUserFriendlyMessage());
        
        // 2. Vibrazione del campo email (come nel login)
        shakeNode(emailField);
        
        // 3. Focus sul campo email
        emailField.requestFocus();
        emailField.selectAll();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setOpacity(1.0);
        errorLabel.setVisible(true);
        
        // Animazione fade out dopo 5 secondi (come nel login)
        if (errorFade != null) errorFade.stop();
        
        errorFade = new FadeTransition(Duration.seconds(0.5), errorLabel);
        errorFade.setFromValue(1.0);
        errorFade.setToValue(0.0);
        errorFade.setDelay(Duration.seconds(5));
        errorFade.setOnFinished(fadeEvent -> errorLabel.setVisible(false));
        errorFade.play();
    }
    
    private void shakeNode(Node node) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), 10)),
            new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), -10)),
            new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 10)),
            new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), 0))
        );
        timeline.play();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean validatePasswordLength() {
        if (passwordField.getText().length() < 8) {
            showError("La password deve essere di almeno 8 caratteri!");
            shakeNode(passwordField);
            return false;
        }
        return true;
    }

    private boolean validatePasswordMatch() {
        if (!passwordField.getText().equals(repeatPasswordField.getText())) {
            showError("Le password non coincidono!");
            shakeNode(passwordField);
            shakeNode(repeatPasswordField);
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }
    
    private boolean validateAllFields() {
        if (!validateMandatoryFields()) return false;
        if (!validateEmail()) return false;
        if (!validatePasswordLength()) return false;
        return validatePasswordMatch();
    }
    
    private boolean validateMandatoryFields() {

        if (firstNameField.getText().trim().isEmpty()) {
            showErrorAndShake("Il nome è obbligatorio!", firstNameField);
            return false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            showErrorAndShake("Il cognome è obbligatorio!", lastNameField);
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showErrorAndShake("L'email è obbligatoria!", emailField);
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            showErrorAndShake("La password è obbligatoria!", passwordField);
            return false;
        }

        if (repeatPasswordField.getText().isEmpty()) {
            showErrorAndShake("Devi ripetere la password!", repeatPasswordField);
            return false;
        }

        return true;
    }
    
    private boolean validateEmail() {
        if (!isValidEmail(emailField.getText())) {
            showErrorAndShake("Inserisci un'email valida!", emailField);
            return false;
        }
        return true;
    }
    
    private void showErrorAndShake(String message, Node node) {
        showError(message);
        shakeNode(node);
    }

    @FXML
    private void handleBack() {
        stateManager.goBack(); // Standard back per Auth states
    }
}