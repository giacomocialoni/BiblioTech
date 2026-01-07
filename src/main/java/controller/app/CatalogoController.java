package controller.app;

import java.util.List;
import java.util.stream.Collectors;

import bean.BookBean;
import dao.BookDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import model.Book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogoController {

    private static final Logger logger =
            LoggerFactory.getLogger(CatalogoController.class);

    private final BookDAO bookDAO;

    public CatalogoController() {
        this.bookDAO = DAOFactory.getInstance().getBookDAO();
    }

    // ================== READ ==================

    public List<BookBean> getAllBooks() {
        try {
            return bookDAO.getAllBooks()
                    .stream()
                    .map(this::toBookBean)
                    .collect(Collectors.toList());

        } catch (DAOException e) {
            logger.error("Errore DAO durante il recupero del catalogo libri", e);
            return List.of();
        }
    }

    // ================== MAPPING ==================

    private BookBean toBookBean(Book book) {
        try {
            BookBean bean = new BookBean();
            bean.setId(book.getId());
            bean.setTitle(book.getTitle());
            bean.setAuthor(book.getAuthor());
            bean.setCategory(book.getCategory());
            bean.setStock(book.getStock());

            if (book.getImagePath() != null) {
                bean.setImagePath(book.getImagePath());
            }

            return bean;

        } catch (IncorrectDataException e) {
            logger.error("Dati non validi nel Book id={}", book.getId(), e);
            return null;
        }
    }
}