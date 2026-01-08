package controller.observer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import model.Book;

class WishlistObserverTest {

    @Test
    void testObserverIsNotified() {
        WishlistObservable observable = new WishlistObservable();

        // Observer finto che memorizza se è stato chiamato
        final boolean[] wasNotified = {false};

        WishlistObserver mockObserver = (book) -> wasNotified[0] = true;

        observable.addObserver(mockObserver);

        Book fakeBook = Book.builder()
                .id(1)
                .title("Test Title") // AGGIUNTO: titolo richiesto
                .author("Test Author") // AGGIUNTO: autore richiesto
                .stock(3)
                .build();

        observable.notifyBookAvailable(fakeBook);

        assertTrue(wasNotified[0], "L'observer avrebbe dovuto essere notificato ma non lo è stato!");
    }
}