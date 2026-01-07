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
import bean.BookBean;
import bean.PurchaseBean;
import bean.LoanBean;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationCardFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReservationCardFactory.class);
    
    public HBox createPurchaseCard(PurchaseBean purchase, BookBean book, Runnable onAccept, Runnable onReject) {
        return createReservationCard(
            book,
            purchase.getUserEmail(),
            "Vendita",
            "Quantità: 1",
            "Prezzo: €" + String.format("%.2f", book.getPrice()),
            onAccept,
            onReject
        );
    }

    public HBox createLoanCard(LoanBean loan, BookBean book, Runnable onAccept, Runnable onReject) {
        return createReservationCard(
            book,
            loan.getUserEmail(),
            "Prestito",
            "Durata: 30 giorni",
            "Data prenotazione: " + loan.getReservedDate(),
            onAccept,
            onReject
        );
    }

    private HBox createReservationCard(BookBean book, String userEmail, String type, 
                                     String detail1, String detail2, Runnable onAccept, Runnable onReject) {
        
        ImageView coverImage = createCoverImage(book);
        VBox imageContainer = createImageContainer(coverImage);
        VBox infoBox = createInfoBox(book, userEmail, type, detail1, detail2, onAccept, onReject);

        HBox card = new HBox(0);
        card.getStyleClass().add("reservation-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(imageContainer, infoBox);
        
        return card;
    }

    private ImageView createCoverImage(BookBean book) {
        String imagePath = "/images/" + book.getImagePath();
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        
        if (imageStream == null) {
            logger.warn("Immagine non trovata: {}", imagePath);
            imageStream = getClass().getResourceAsStream("/images/default.jpg");
        }
        
        try (InputStream stream = imageStream) {
            if (stream != null) {
                ImageView coverImage = new ImageView(new Image(stream));
                coverImage.setFitWidth(120);
                coverImage.setFitHeight(160);
                coverImage.setPreserveRatio(true);
                
                Rectangle clip = new Rectangle(coverImage.getFitWidth(), coverImage.getFitHeight());
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                coverImage.setClip(clip);
                
                return coverImage;
            }
        } catch (Exception e) {
            logger.error("Errore caricamento immagine: {}", e.getMessage());
        }
        
        return new ImageView();
    }

    private VBox createImageContainer(ImageView coverImage) {
        VBox imageContainer = new VBox(coverImage);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(15));
        imageContainer.setMinWidth(150);
        imageContainer.setStyle("-fx-background-color: #faf8f5; -fx-background-radius: 10; -fx-border-radius: 10;");

        Rectangle containerClip = new Rectangle(150, 190);
        containerClip.setArcWidth(20);
        containerClip.setArcHeight(20);
        imageContainer.setClip(containerClip);
        
        return imageContainer;
    }

    private VBox createInfoBox(BookBean book, String userEmail, String type, 
                             String detail1, String detail2, Runnable onAccept, Runnable onReject) {
        
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(20));
        infoBox.setAlignment(Pos.TOP_LEFT);
        
        Label userLabel = new Label("Utente: " + userEmail);
        userLabel.getStyleClass().add("reservation-user");
        
        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("reservation-title");
        
        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.getStyleClass().add("reservation-author");
        
        Label detail1Label = new Label(detail1);
        detail1Label.getStyleClass().add("reservation-detail");
        
        Label detail2Label = new Label(detail2);
        detail2Label.getStyleClass().add("reservation-detail");
        
        HBox buttonBox = createButtonBox(type, onAccept, onReject);
        
        infoBox.getChildren().addAll(
            userLabel, titleLabel, authorLabel, 
            detail1Label, detail2Label, buttonBox
        );
        
        return infoBox;
    }

    private HBox createButtonBox(String type, Runnable onAccept, Runnable onReject) {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button acceptButton = new Button(type);
        acceptButton.getStyleClass().add("buy-button");
        acceptButton.setOnAction(e -> onAccept.run());
        
        Button rejectButton = new Button("Rifiuta");
        rejectButton.getStyleClass().add("borrow-button");
        rejectButton.setOnAction(e -> onReject.run());
        
        buttonBox.getChildren().addAll(acceptButton, rejectButton);
        
        return buttonBox;
    }
}