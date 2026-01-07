package controller.app;

import bean.BookBean;
import bean.UserBean;
import dao.BookDAO;
import dao.UserDAO;
import dao.WishlistDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import exception.RecordNotFoundException;
import model.Book;
import model.User;
import model.Wishlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WishlistController {

    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    private final UserDAO userDAO;
    private final WishlistDAO wishlistDAO;
    private final BookDAO bookDAO;

    public WishlistController() {
        DAOFactory factory = DAOFactory.getInstance();
        this.userDAO = factory.getUserDAO();
        this.wishlistDAO = factory.getWishlistDAO();
        this.bookDAO = factory.getBookDAO();
    }

    /* =====================
       USER (BEAN)
       ===================== */
    public UserBean getUser(String email) {
        try {
            User user = userDAO.getUser(email);
            return toUserBean(user);
        } catch (RecordNotFoundException e) {
            logger.warn("Utente non trovato: {}", email);
            return null;
        } catch (DAOException e) {
            logger.error("Errore DAO recupero utente {}", email, e);
            return null;
        }
    }

    /* =====================
       WISHLIST (BEAN)
       ===================== */
    public List<BookBean> getWishlistBooks(String email) {
        List<BookBean> beans = new ArrayList<>();

        try {
            List<Wishlist> wishlist = wishlistDAO.getWishlistByUser(email);

            for (Wishlist w : wishlist) {
                try {
                    Book book = bookDAO.getBookById(w.getBookId());
                    BookBean bean = toBookBean(book);
                    if (bean != null) beans.add(bean);

                } catch (RecordNotFoundException e) {
                    logger.warn("Libro non trovato id={}", w.getBookId());
                } catch (DAOException e) {
                    logger.error("Errore DAO recupero libro id={}", w.getBookId(), e);
                }
            }

        } catch (DAOException e) {
            logger.error("Errore DAO recupero wishlist utente {}", email, e);
        }

        return beans;
    }

    /* =====================
       COMMANDS
       ===================== */
    public boolean addToWishlist(String email, int bookId) {
        try {
            wishlistDAO.addToWishlist(email, bookId);
            return true;
        } catch (DAOException e) {
            logger.error("Errore DAO aggiunta libro id={} wishlist {}", bookId, email, e);
            return false;
        }
    }

    public boolean removeFromWishlist(String email, int bookId) {
        try {
            wishlistDAO.removeFromWishlist(email, bookId);
            return true;
        } catch (DAOException e) {
            logger.error("Errore DAO rimozione libro id={} wishlist {}", bookId, email, e);
            return false;
        }
    }

    /* =====================
       MAPPING PRIVATO
       ===================== */
    private UserBean toUserBean(User user) {
        UserBean bean = new UserBean();
        bean.setEmail(user.getEmail());
        bean.setPassword(user.getPassword());
        bean.setFirstName(user.getFirstName());
        bean.setLastName(user.getLastName());
        return bean;
    }

    private BookBean toBookBean(Book book) {
        BookBean bean = new BookBean();
        try {
			bean.setId(book.getId());
			bean.setTitle(book.getTitle());
			bean.setAuthor(book.getAuthor());
			bean.setCategory(book.getCategory());
			bean.setImagePath(book.getImagePath());
			bean.setStock(book.getStock());
			bean.setPrice(book.getPrice());
		} catch (IncorrectDataException e) {
            logger.error("Errore nella trasformazione di book bean.", e);
		}
        return bean;
    }
}