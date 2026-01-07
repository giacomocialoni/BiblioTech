package view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import utils.LoanStatus;
import bean.BookBean;
import bean.LoanBean;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturningLoanCardFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReturningLoanCardFactory.class);
    
    public HBox createLoanCard(LoanBean loan, BookBean book, Runnable onReturn) {
        ImageView coverImage = createBookCover(book);
        VBox imageContainer = createImageContainer(coverImage);
        VBox infoBox = createInfoBox(loan, book, onReturn);

        HBox card = new HBox(10);
        card.getStyleClass().add("loan-card-container");
        card.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(imageContainer, infoBox);

        return card;
    }

    private ImageView createBookCover(BookBean book) {
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(100);
        coverImage.setFitHeight(140);
        coverImage.setPreserveRatio(true);

        String imagePath = "/images/" + book.getImagePath();
        try (InputStream imageStream = getClass().getResourceAsStream(imagePath)) {
            InputStream stream = imageStream;
            if (stream == null) {
                stream = getClass().getResourceAsStream("/images/default.jpg");
            }
            if (stream != null) {
                coverImage.setImage(new Image(stream));
                Rectangle clip = new Rectangle(coverImage.getFitWidth(), coverImage.getFitHeight());
                clip.setArcWidth(15);
                clip.setArcHeight(15);
                coverImage.setClip(clip);
            }
        } catch (Exception e) {
            logger.error("Errore caricamento immagine: {}", e.getMessage());
        }

        return coverImage;
    }

    private VBox createImageContainer(ImageView coverImage) {
        VBox imageContainer = new VBox(coverImage);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(10));
        imageContainer.setMinWidth(120);
        imageContainer.setStyle("-fx-background-color: #faf8f5; -fx-background-radius: 10; -fx-border-radius: 10;");
        return imageContainer;
    }

    private VBox createInfoBox(LoanBean loan, BookBean book, Runnable onReturn) {
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10, 15, 10, 15));
        infoBox.setAlignment(Pos.TOP_LEFT);

        Label userLabel = new Label("Utente: " + loan.getUserEmail());
        userLabel.getStyleClass().add("reservation-user");

        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("reservation-title");

        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.getStyleClass().add("reservation-author");

        Label detail1Label = new Label("Prestito in corso");
        detail1Label.getStyleClass().add("reservation-detail");

        Label detail2Label = new Label("Data prestito: " + loan.getLoanedDate());
        detail2Label.getStyleClass().add("reservation-detail");

        Label remainingLabel = createRemainingDaysLabel(loan);

        Button returnButton = new Button("Contrassegna come restituito");
        returnButton.getStyleClass().add("buy-button");
        returnButton.setOnAction(e -> onReturn.run());
        returnButton.setMaxWidth(Double.MAX_VALUE);

        infoBox.getChildren().addAll(
            userLabel, titleLabel, authorLabel, detail1Label, 
            detail2Label, remainingLabel, returnButton
        );
        
        return infoBox;
    }

    private Label createRemainingDaysLabel(LoanBean loan) {
        Label remainingLabel = new Label();
        if (loan.getReturningDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), loan.getReturningDate());

            if (loan.getStatus() == LoanStatus.EXPIRED || daysRemaining < 0) {
                remainingLabel.setText("Prestito scaduto da " + Math.abs(daysRemaining) + " giorni");
                remainingLabel.getStyleClass().add("loan-expired");
            } else if (daysRemaining <= 3) {
                remainingLabel.setText("Giorni rimanenti: " + daysRemaining);
                remainingLabel.getStyleClass().add("loan-warning");
            } else {
                remainingLabel.setText("Giorni rimanenti: " + daysRemaining);
                remainingLabel.getStyleClass().add("loan-normal");
            }
        }
        return remainingLabel;
    }
}