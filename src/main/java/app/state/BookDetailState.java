package app.state;

import app.StageManagerGUI;
import controller.gui.BookDetailControllerGUI;

/**
 * Stato secondario per i dettagli di un libro.
 * Estende SecondaryState e carica BookDetailView.fxml.
 * Quando si preme "back", si torna al PrimaryState precedente (Catalogo o Cerca).
 */
public class BookDetailState extends DetailState {
    private final int bookId;
    
    public BookDetailState(StateManager stateManager, int bookId) {
        super(stateManager);
        this.bookId = bookId;
    }
    
    @Override
    public void onEnter() {
        BookDetailControllerGUI controller = stateManager.getStageManager()
            .<BookDetailControllerGUI>loadContent(StageManagerGUI.BOOK_DETAIL_VIEW);
        if (controller != null) {
            controller.setStateManager(stateManager);
            controller.loadBook(bookId);
        }
    }
}