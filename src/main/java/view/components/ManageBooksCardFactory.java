package view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import bean.BookBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.function.Consumer;

public class ManageBooksCardFactory {

    private static final Logger logger = LoggerFactory.getLogger(ManageBooksCardFactory.class);
    private static final String BOOK_DETAIL = "book-detail";

    public HBox createBookCard(BookBean book, Consumer<Integer> onIncreaseStock, 
                              Consumer<Integer> onDecreaseStock, Runnable onRemoveBook) {
        
        // Copertina del libro a sinistra (layout originale)
        ImageView coverImage = createBookCover(book);
        
        // Container per l'immagine
        VBox imageContainer = createImageContainer(coverImage);
        
        // Informazioni del libro
        VBox infoBox = createBookInfo(book);
        
        // Controlli stock
        VBox controlsBox = createStockControls(book, onIncreaseStock, onDecreaseStock, onRemoveBook);
        
        // Card principale - HBox come prima

        HBox card = new HBox(20); // Spacing normale
        card.getStyleClass().add("manage-book-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.getChildren().addAll(imageContainer, infoBox, controlsBox);
        
        return card;
    }

    private ImageView createBookCover(BookBean book) {
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(100); // Ripristinato dimensione normale
        coverImage.setFitHeight(140);
        coverImage.setPreserveRatio(true);
        
        try {
            String imagePath = "/images/" + book.getImagePath();
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream == null) {
                imageStream = getClass().getResourceAsStream("/images/default.jpg");
            }
            
            if (imageStream != null) {
                Image image = new Image(imageStream);
                coverImage.setImage(image);
                
                Rectangle clip = new Rectangle(coverImage.getFitWidth(), coverImage.getFitHeight());
                clip.setArcWidth(15);
                clip.setArcHeight(15);
                coverImage.setClip(clip);
                
                imageStream.close();
            } else {
                coverImage.setStyle("-fx-background-color: #e8dad0; -fx-border-color: #8b7355;");
            }
        } catch (Exception e) {
            logger.error("Errore nel caricamento dell'immagine per il libro: {}", book.getTitle(), e);
            coverImage.setStyle("-fx-background-color: #e8dad0; -fx-border-color: #8b7355;");
        }
        
        return coverImage;
    }

    private VBox createImageContainer(ImageView coverImage) {
        VBox imageContainer = new VBox(coverImage);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(10)); // Padding normale
        imageContainer.setMinWidth(120); // Ripristinato dimensione normale
        imageContainer.setStyle("-fx-background-color: #faf8f5; -fx-background-radius: 10; -fx-border-radius: 10;");
        
        return imageContainer;
    }

    private VBox createBookInfo(BookBean book) {
    	VBox infoBox = new VBox(8); // Spacing normale
        infoBox.setPadding(new Insets(10)); // Padding normale
        infoBox.setAlignment(Pos.TOP_LEFT);
        infoBox.setPrefWidth(300);
        
        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("book-title");
        titleLabel.setWrapText(true);
        
        Label authorLabel = new Label("di " + book.getAuthor());
        authorLabel.getStyleClass().add("book-author");
        
        Label isbnLabel = new Label("ISBN: " + book.getIsbn());
        isbnLabel.getStyleClass().add(BOOK_DETAIL);
        
        Label categoryLabel = new Label("Categoria: " + book.getCategory());
        categoryLabel.getStyleClass().add(BOOK_DETAIL);
        
        Label yearLabel = new Label("Anno: " + book.getYear());
        yearLabel.getStyleClass().add(BOOK_DETAIL);
        
        Label priceLabel = new Label("Prezzo: €" + String.format("%.2f", book.getPrice()));
        priceLabel.getStyleClass().add("book-price");
        
        // Stock migliorato - stile più evidente
        Label stockLabel = new Label("Stock attuale: " + book.getStock());
        stockLabel.getStyleClass().add("manage-stock-label");
        
        infoBox.getChildren().addAll(
            titleLabel, authorLabel, isbnLabel, 
            categoryLabel, yearLabel, stockLabel, priceLabel
        );
        
        return infoBox;
    }

    private VBox createStockControls(BookBean book, Consumer<Integer> onIncreaseStock, Consumer<Integer> onDecreaseStock, Runnable onRemoveBook) {
    	VBox controlsBox = new VBox(15); // Spacing normale
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10)); // Larghezza fissa per controlli
        
        // Spinner per quantità
        VBox quantityBox = new VBox(5);
        quantityBox.setAlignment(Pos.CENTER);
        
        Label quantityLabel = new Label("Quantità:");
        quantityLabel.getStyleClass().add("control-label");
        
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.getStyleClass().add("quantity-spinner");
        
        quantityBox.getChildren().addAll(quantityLabel, quantitySpinner);
        
        // Pulsanti azione stock (stile originale)
        HBox stockButtons = new HBox(10);
        stockButtons.setAlignment(Pos.CENTER);
        
        Button addButton = new Button("Aggiungi");
        addButton.getStyleClass().add("buy-button");
        addButton.setOnAction(e -> {
            int quantity = quantitySpinner.getValue();
            onIncreaseStock.accept(quantity);
        });
        
        Button sellButton = new Button("Vendi");
        sellButton.getStyleClass().add("borrow-button");
        sellButton.setOnAction(e -> {
            int quantity = quantitySpinner.getValue();
            onDecreaseStock.accept(quantity);
        });
        
        stockButtons.getChildren().addAll(addButton, sellButton);
        
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        separator.getStyleClass().add("separator");
        separator.setPrefWidth(200);
        
        // Pulsante elimina libro - largo come i due bottoni insieme
        Button removeButton = new Button("Elimina Libro");
        removeButton.getStyleClass().add("manage-remove-button");
        removeButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setOnAction(e -> onRemoveBook.run());
        
        controlsBox.getChildren().addAll(quantityBox, stockButtons, separator, removeButton);
        
        return controlsBox;
    }
}