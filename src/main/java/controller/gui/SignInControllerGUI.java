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
        boolean valid = true;
        
        // Controllo campi obbligatori
        if (firstNameField.getText().trim().isEmpty()) {
            showError("Il nome è obbligatorio!");
            shakeNode(firstNameField);
            valid = false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            if (valid) showError("Il cognome è obbligatorio!");
            shakeNode(lastNameField);
            valid = false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            if (valid) showError("L'email è obbligatoria!");
            shakeNode(emailField);
            valid = false;
        }
        
        if (passwordField.getText().isEmpty()) {
            if (valid) showError("La password è obbligatoria!");
            shakeNode(passwordField);
            valid = false;
        }
        
        if (repeatPasswordField.getText().isEmpty()) {
            if (valid) showError("Devi ripetere la password!");
            shakeNode(repeatPasswordField);
            valid = false;
        }
        
        // Validazione email format
        if (valid && !isValidEmail(emailField.getText())) {
            showError("Inserisci un'email valida!");
            shakeNode(emailField);
            valid = false;
        }
        
        // Validazione password
        if (valid && !validatePasswordLength()) {
            valid = false;
        }
        
        // Validazione password match
        if (valid && !validatePasswordMatch()) {
            valid = false;
        }
        
        return valid;
    }

    @FXML
    private void handleBack() {
        stateManager.goBack(); // Standard back per Auth states
    }
}