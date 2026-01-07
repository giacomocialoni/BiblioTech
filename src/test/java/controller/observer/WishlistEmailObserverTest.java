package controller.observer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import model.Book;
import model.User;
import model.Wishlist;
import service.EmailService;
import dao.WishlistDAO;
import exception.DAOException;

class WishlistEmailObserverTest {

    // Fake email service che salva le email inviate
    static class FakeEmailService extends EmailService {
        List<String> sentTo = new ArrayList<>();
        List<String> subjects = new ArrayList<>();
        List<String> bodies = new ArrayList<>();

        @Override
        public boolean send(String to, String subject, String body) {
            sentTo.add(to);
            subjects.add(subject);
            bodies.add(body);
            return true;
        }
    }

    // Mock DAO che restituisce utenti finti
    static class MockWishlistDAO implements WishlistDAO {
        @Override
        public List<User> getUsersWithBookInWishlist(int bookId) throws DAOException {
            // Crea e restituisce una lista di User invece di String
            return List.of(
                new User("test1@example.com", "password1", "Mario", "Rossi"),
                new User("test2@example.com", "password2", "Luigi", "Verdi")
            );
        }

        @Override
        public void addToWishlist(String userEmail, int bookId) throws DAOException {
            // do nothing
        }

        @Override
        public void removeFromWishlist(String userEmail, int bookId) throws DAOException {
            // do nothing
        }

        @Override
        public boolean isInWishlist(String userEmail, int bookId) throws DAOException {
            // do nothing
            return true;
        }

        @Override
        public List<Wishlist> getWishlistByUser(String userEmail) throws DAOException {
            // do nothing
            return null;
        }
    }

    @Test
    void testEmailObserverSendsEmails() {
        FakeEmailService fakeEmailService = new FakeEmailService();
        MockWishlistDAO mockDAO = new MockWishlistDAO();

        WishlistEmailObserver observer = new WishlistEmailObserver(mockDAO, fakeEmailService);

        Book book = new Book();
        book.setId(123);
        book.setTitle("Il Signore degli Anelli");
        book.setAuthor("J.R.R. Tolkien");

        observer.onBookAvailable(book);

        // Verifica: devono essere inviate due mail
        assertEquals(2, fakeEmailService.sentTo.size());
        assertTrue(fakeEmailService.sentTo.contains("test1@example.com"));
        assertTrue(fakeEmailService.sentTo.contains("test2@example.com"));

        // Verifica il subject
        assertTrue(fakeEmailService.subjects.get(0).contains("Buone notizie da Bibliotech"));

        // Verifica corpo mail - ora dovrebbe contenere i nomi degli utenti
        String body1 = fakeEmailService.bodies.get(0);
        String body2 = fakeEmailService.bodies.get(1);
        
        assertTrue(body1.contains("Ciao Mario,") || body1.contains("Ciao Luigi,"));
        assertTrue(body2.contains("Ciao Mario,") || body2.contains("Ciao Luigi,"));
        assertTrue(body1.contains("Il Signore degli Anelli"));
        assertTrue(body1.contains("J.R.R. Tolkien"));
        assertTrue(body2.contains("Il Signore degli Anelli"));
        assertTrue(body2.contains("J.R.R. Tolkien"));
    }
}