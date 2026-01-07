package controller.gui;

import app.state.ErrorState;
import app.state.StateManager;
import app.state.SuccessState;
import bean.BookBean;
import controller.app.ManageBooksController;
import exception.DuplicateBookException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class CreateBookControllerGUI {

    private static final Logger logger =
            LoggerFactory.getLogger(CreateBookControllerGUI.class);

    private static final String IMAGE_DIR = "src/main/resources/images";

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField yearField;
    @FXML private TextField publisherField;
    @FXML private TextField pagesField;
    @FXML private TextField isbnField;
    @FXML private TextField stockField;
    @FXML private TextArea plotArea;
    @FXML private TextField priceField;
    @FXML private ImageView previewImage;
    @FXML private Label imageLabel;

    private StateManager stateManager;
    private final ManageBooksController appController = new ManageBooksController();
    private File selectedImageFile;

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @FXML
    public void initialize() {
        loadCategories();
        loadPlaceholderImage();
        allowOnlyNumbers(yearField);
        allowOnlyNumbers(pagesField);
        allowOnlyNumbers(stockField);
        allowDecimal(priceField);
    }

    private void loadCategories() {
        try {
            List<String> categories = appController.getAllCategoryNames();
            categoryCombo.getItems().clear();
            categoryCombo.getItems().addAll(categories);
            if (!categories.isEmpty()) {
                categoryCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading categories", e);
        }
    }

    private void loadPlaceholderImage() {
        try {
            previewImage.setImage(
                new Image(getClass().getResourceAsStream("/images/placeholder.png"))
            );
        } catch (Exception e) {
            logger.warn("Placeholder image not found");
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select book image");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            previewImage.setImage(new Image(file.toURI().toString()));
            imageLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleCreateBook() {
        if (!validateFields()) return;

        String imageName = null;

        try {
            if (selectedImageFile != null) {
                imageName = copyImageToResources(selectedImageFile, titleField.getText());
            }

            BookBean bean = new BookBean();
            bean.setTitle(titleField.getText().trim());
            bean.setAuthor(authorField.getText().trim());
            bean.setCategory(categoryCombo.getValue());
            bean.setYear(Integer.parseInt(yearField.getText()));
            bean.setPublisher(publisherField.getText().trim());
            bean.setPages(Integer.parseInt(pagesField.getText()));
            bean.setIsbn(isbnField.getText().trim());
            bean.setStock(Integer.parseInt(stockField.getText()));
            bean.setPlot(plotArea.getText().trim());
            bean.setPrice(Double.parseDouble(priceField.getText()));
            bean.setImagePath(imageName);

            appController.addBook(bean);

            stateManager.setState(new SuccessState(
                stateManager,
                "Book '" + bean.getTitle() + "' created successfully"
            ));

        } catch (DuplicateBookException e) {
            logger.warn("Duplicate book creation attempt", e);
            stateManager.setState(new ErrorState(
                stateManager,
                e.getUserFriendlyMessage()
            ));
        } catch (Exception e) {
            logger.error("Error creating book", e);
            stateManager.setState(new ErrorState(
                stateManager,
                "Error creating book"
            ));
        }
    }

    private String copyImageToResources(File source, String title) throws Exception {
        Files.createDirectories(Path.of(IMAGE_DIR));

        String extension = source.getName()
                .substring(source.getName().lastIndexOf('.'));

        String fileName = title
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .trim()
                .replace(" ", "_") + extension;

        Path target = Path.of(IMAGE_DIR, fileName);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Image saved in resources: {}", fileName);
        return fileName;
    }

    private boolean validateFields() {
        if (titleField.getText().isBlank()
                || authorField.getText().isBlank()
                || categoryCombo.getValue() == null
                || yearField.getText().isBlank()
                || pagesField.getText().isBlank()
                || stockField.getText().isBlank()
                || priceField.getText().isBlank()) {

            stateManager.setState(new ErrorState(
                stateManager,
                "All mandatory fields must be filled"
            ));
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancel() {
        stateManager.goBack();
    }

    private void allowOnlyNumbers(TextField field) {
        field.textProperty().addListener((o, old, val) -> {
            if (!val.matches("\\d*")) {
                field.setText(val.replaceAll("\\D", ""));
            }
        });
    }

    private void allowDecimal(TextField field) {
        field.textProperty().addListener((o, old, val) -> {
            if (!val.matches("\\d*(\\.\\d*)?")) {
                field.setText(old);
            }
        });
    }
}