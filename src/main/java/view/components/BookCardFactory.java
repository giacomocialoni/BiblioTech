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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookCardFactory {

    private static final Logger logger = LoggerFactory.getLogger(BookCardFactory.class);
    private final StateManager stateManager;
    private static final String UNAVAILABLE_STYLE_CLASS = "unavailable";
    private static final String UNAVAILABLE_BANNER_STYLE_CLASS = "unavailable-banner";

    public BookCardFactory(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public StackPane createBookCard(BookBean book) {
        VBox contentBox = new VBox(8);
        contentBox.getStyleClass().add("book-card");
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPrefSize(200, 300);
        contentBox.setPadding(new Insets(15));

        ImageView cover = new ImageView();
        cover.setFitWidth(150);
        cover.setFitHeight(180);
        cover.setPreserveRatio(true);
        Image image = loadBookImage("/images/" + book.getImagePath());
        if (image != null) cover.setImage(image);

        cover.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.2)));

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

        StackPane card = new StackPane(contentBox);
        card.setPrefSize(200, 300);
        card.setAlignment(Pos.CENTER);

        if (book.getStock() <= 0) {
            applyUnavailableStyles(contentBox, titleLabel, authorLabel, genreLabel, cover);
            
            Label banner = new Label("NON DISPONIBILE");
            banner.getStyleClass().add(UNAVAILABLE_BANNER_STYLE_CLASS);
            banner.setPrefWidth(200);
            banner.setPrefHeight(20);
            card.getChildren().add(banner);
        }

        card.setOnMouseClicked(e -> stateManager.setState(new BookDetailState(stateManager, book.getId())));

        return card;
    }

    private void applyUnavailableStyles(VBox contentBox, Label titleLabel, 
                                       Label authorLabel, Label genreLabel, ImageView cover) {
        contentBox.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);
        titleLabel.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);
        authorLabel.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);
        genreLabel.getStyleClass().add(UNAVAILABLE_STYLE_CLASS);

        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        cover.setEffect(grayscale);
    }

    private Image loadBookImage(String path) {
        try (var stream = getClass().getResourceAsStream(path)) {
            if (stream != null) return new Image(stream);

            logger.warn("Immagine non trovata: {}. Uso default.jpg", path);
            try (var defaultStream = getClass().getResourceAsStream("/images/default.jpg")) {
                return new Image(defaultStream);
            }
        } catch (Exception e) {
            logger.error("Errore caricamento immagine: {}", path, e);
            return null;
        }
    }
}