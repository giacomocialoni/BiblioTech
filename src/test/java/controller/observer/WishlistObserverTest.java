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

        Book fakeBook = new Book();
        fakeBook.setId(1);
        fakeBook.setStock(3);

        observable.notifyBookAvailable(fakeBook);

        assertTrue(wasNotified[0], "L'observer avrebbe dovuto essere notificato ma non lo è stato!");
    }
}