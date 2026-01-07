package view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.IntConsumer;

public class ManageBooksCardFactory {

    private static final Logger logger = LoggerFactory.getLogger(ManageBooksCardFactory.class);
    private static final String BOOK_DETAIL = "book-detail";
    private static final String IMAGE_DIR = "/images/";

    /**
     * Crea una card HBox per un libro.
     *
     * @param book             Il libro da visualizzare
     * @param onIncreaseStock  Funzione chiamata per aumentare stock (int)
     * @param onDecreaseStock  Funzione chiamata per diminuire stock (int)
     * @param onRemoveBook     Runnable chiamato per rimuovere il libro
     * @return HBox card
     */
    public HBox createBookCard(BookBean book, IntConsumer onIncreaseStock,
                               IntConsumer onDecreaseStock, Runnable onRemoveBook) {

        ImageView coverImage = createBookCover(book);
        VBox imageContainer = createImageContainer(coverImage);
        VBox infoBox = createBookInfo(book);
        VBox controlsBox = createStockControls(onIncreaseStock, onDecreaseStock, onRemoveBook);

        HBox card = new HBox(20); // spacing
        card.getStyleClass().add("manage-book-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.getChildren().addAll(imageContainer, infoBox, controlsBox);

        return card;
    }

    private ImageView createBookCover(BookBean book) {
        ImageView coverImage = new ImageView();
        coverImage.setFitWidth(100);
        coverImage.setFitHeight(140);
        coverImage.setPreserveRatio(true);

        try (InputStream imageStream = getClass().getResourceAsStream(IMAGE_DIR + book.getImagePath())) {

            Image image;
            if (imageStream != null) {
                image = new Image(imageStream);
            } else {
                InputStream defaultStream = getClass().getResourceAsStream(IMAGE_DIR + "default.jpg");
                image = defaultStream != null ? new Image(defaultStream) : null;
            }

            if (image != null) {
                coverImage.setImage(image);
                Rectangle clip = new Rectangle(coverImage.getFitWidth(), coverImage.getFitHeight());
                clip.setArcWidth(15);
                clip.setArcHeight(15);
                coverImage.setClip(clip);
            } else {
                coverImage.setStyle("-fx-background-color: #e8dad0; -fx-border-color: #8b7355;");
            }

        } catch (Exception e) {
            logger.error("Errore caricamento immagine libro: {}", book.getTitle(), e);
            coverImage.setStyle("-fx-background-color: #e8dad0; -fx-border-color: #8b7355;");
        }

        return coverImage;
    }

    private VBox createImageContainer(ImageView coverImage) {
        VBox container = new VBox(coverImage);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        container.setMinWidth(120);
        container.setStyle("-fx-background-color: #faf8f5; -fx-background-radius: 10; -fx-border-radius: 10;");
        return container;
    }

    private VBox createBookInfo(BookBean book) {
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(10));
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

        Label stockLabel = new Label("Stock attuale: " + book.getStock());
        stockLabel.getStyleClass().add("manage-stock-label");

        infoBox.getChildren().addAll(titleLabel, authorLabel, isbnLabel,
                categoryLabel, yearLabel, stockLabel, priceLabel);

        return infoBox;
    }

    private VBox createStockControls(IntConsumer onIncreaseStock, IntConsumer onDecreaseStock, Runnable onRemoveBook) {
        VBox controlsBox = new VBox(15);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10));

        // Spinner quantità
        VBox quantityBox = new VBox(5);
        quantityBox.setAlignment(Pos.CENTER);
        Label quantityLabel = new Label("Quantità:");
        quantityLabel.getStyleClass().add("control-label");

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.getStyleClass().add("quantity-spinner");

        quantityBox.getChildren().addAll(quantityLabel, quantitySpinner);

        // Pulsanti azione stock
        HBox stockButtons = new HBox(10);
        stockButtons.setAlignment(Pos.CENTER);

        Button addButton = new Button("Aggiungi");
        addButton.getStyleClass().add("buy-button");
        addButton.setOnAction(e -> onIncreaseStock.accept(quantitySpinner.getValue()));

        Button sellButton = new Button("Vendi");
        sellButton.getStyleClass().add("borrow-button");
        sellButton.setOnAction(e -> onDecreaseStock.accept(quantitySpinner.getValue()));

        stockButtons.getChildren().addAll(addButton, sellButton);

        Separator separator = new Separator();
        separator.getStyleClass().add("separator");
        separator.setPrefWidth(200);

        // Pulsante elimina libro
        Button removeButton = new Button("Elimina Libro");
        removeButton.getStyleClass().add("manage-remove-button");
        removeButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setOnAction(e -> onRemoveBook.run());

        controlsBox.getChildren().addAll(quantityBox, stockButtons, separator, removeButton);
        return controlsBox;
    }

    /**
     * Copia un file immagine nella cartella delle risorse (facoltativo se vuoi gestire caricamento custom)
     */
    public static String copyImageToResources(File source, String title, String targetDir) throws Exception {
        Files.createDirectories(Path.of(targetDir));
        String extension = source.getName().substring(source.getName().lastIndexOf('.'));
        String fileName = title.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim().replace(" ", "_") + extension;
        Path target = Path.of(targetDir, fileName);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Image saved in resources: {}", fileName);
        return fileName;
    }
}