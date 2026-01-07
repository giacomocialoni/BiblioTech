package view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import app.state.BookDetailState;
import app.state.StateManager;
import bean.BookBean;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookCardFactory {

    private final StateManager stateManager;
    
    // Costanti per le classi CSS
    private static final String UNAVAILABLE_STYLE_CLASS = "unavailable";
    private static final String UNAVAILABLE_BANNER_STYLE_CLASS = "unavailable-banner";
    private static final Logger logger = LoggerFactory.getLogger(ManageBooksCardFactory.class);

    public BookCardFactory(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public StackPane createBookCard(BookBean book) {
        // Contenuto principale della card
        VBox contentBox = new VBox(8);
        contentBox.getStyleClass().add("book-card");
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPrefSize(200, 300);
        contentBox.setPadding(new Insets(15));

        // Immagine copertina
        String imagePath = "/images/" + book.getImagePath();
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            logger.error("Immagine non trovata: " + imagePath);
            imageStream = getClass().getResourceAsStream("/images/default.jpg");
        }

        ImageView cover = new ImageView(new Image(imageStream));
        cover.setFitWidth(150);
        cover.setFitHeight(180);
        cover.setPreserveRatio(true);
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setColor(Color.rgb(0,0,0,0.2));
        
        // Etichette
        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("book-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        Label authorLabel = new Label(book.getAuthor());
        authorLabel.getStyleClass().add("book-author");
        authorLabel.setWrapText(true);
        authorLabel.setMaxWidth(180);
        authorLabel.setTextAlignment(TextAlignment.CENTER);

        Label genreLabel = new Label(book.getCategory());
        genreLabel.getStyleClass().add("book-genre");
        genreLabel.setWrapText(true);
        genreLabel.setMaxWidth(180);
        genreLabel.setAlignment(Pos.CENTER);

        contentBox.getChildren().addAll(cover, titleLabel, authorLabel, genreLabel);

        // StackPane per gestire la sovrapposizione
        StackPane card = new StackPane(contentBox);
        card.setPrefSize(200, 300);
        card.setAlignment(Pos.CENTER);

        // Se non disponibile
        if (book.getStock() <= 0) {
            contentBox.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);
            titleLabel.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);
            authorLabel.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);
            genreLabel.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);
            
            ColorAdjust grayscale = new ColorAdjust();
            grayscale.setSaturation(-1.0);  // -1 = completamente bianco e nero
            cover.setEffect(grayscale);

            // Banner rosso sopra
            Label unavailableBanner = new Label("NON DISPONIBILE");
            unavailableBanner.getStyleClass().add(UNAVAILABLE_BANNER_STYLE_CLASS);
            unavailableBanner.setPrefWidth(200);
            unavailableBanner.setPrefHeight(20);

            // Aggiungi sopra al StackPane
            card.getChildren().add(unavailableBanner);
        }

        // Clickabile anche se non disponibile
        card.setOnMouseClicked(e ->
                stateManager.setState(new BookDetailState(stateManager, book.getId()))
        );

        return card;
    }
}