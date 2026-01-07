package view.components;

import controller.gui.BookDetailControllerGUI;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import bean.BookBean;

import java.io.InputStream;

public final class BookDetailFactory {

    // Costruttore privato per impedire istanziazione
    private BookDetailFactory() {
        throw new UnsupportedOperationException("Classe utility non istanziabile");
    }

    public static void populateBookDetails(BookDetailControllerGUI controller, BookBean book) {
        controller.getTitleLabel().setText(book.getTitle());
        controller.getAuthorLabel().setText("di " + book.getAuthor());
        controller.getGenreLabel().setText(book.getCategory());
        controller.getYearLabel().setText(String.valueOf(book.getYear()));
        controller.getPublisherLabel().setText(book.getPublisher());
        controller.getPagesLabel().setText(String.valueOf(book.getPages()));
        controller.getIsbnLabel().setText(book.getIsbn());
        controller.getPlotLabel().setText(book.getPlot());

        String imagePath = "/images/" + book.getImagePath();
        InputStream imageStream = BookDetailFactory.class.getResourceAsStream(imagePath);
        if (imageStream == null) {
            imageStream = BookDetailFactory.class.getResourceAsStream("/images/default.jpg");
        }
        controller.getCoverImage().setImage(new Image(imageStream));

        if (book.getStock() > 0) {
            controller.getAvailabilityLabel().setText("Disponibile (" + book.getStock() + " copie)");
            controller.getAvailabilityLabel().setStyle("-fx-text-fill: #4a7c59;");
            controller.getQuantitySpinner().setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, book.getStock(), 1)
            );
            controller.getQuantitySpinner().setDisable(false);
        } else {
            controller.getAvailabilityLabel().setText("Non disponibile");
            controller.getAvailabilityLabel().setStyle("-fx-text-fill: #a94442;");

            // Copertina in bianco e nero
            ColorAdjust grayscale = new ColorAdjust();
            grayscale.setSaturation(-1.0);
            controller.getCoverImage().setEffect(grayscale);

            // Spinner a 0 e disabilitato
            controller.getQuantitySpinner().setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0)
            );
            controller.getQuantitySpinner().setDisable(true);
        }
    }
}