package controller.app;

import dao.BookDAO;
import dao.LoanDAO;
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.LoanResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

class LoanControllerTest {

    private BookDAO bookDAO;
    private LoanDAO loanDAO;
    private LoanController controller;

    @BeforeEach
    void setUp() {
        bookDAO = mock(BookDAO.class);
        loanDAO = mock(LoanDAO.class);

        controller = new LoanController(bookDAO, loanDAO);
    }

    @Test
    void loanBook_success() throws Exception {
        String userEmail = "test@mail.com";
        int bookId = 1;

        Book book = new Book();
        book.setId(bookId);
        book.setStock(3);

        when(bookDAO.getBookById(bookId)).thenReturn(book);
        when(loanDAO.getActiveLoansByUser(userEmail)).thenReturn(List.of());

        LoanResult result = controller.loanBook(userEmail, bookId);

        assertEquals(LoanResult.SUCCESS, result);

        verify(bookDAO).updateBook(book);
        verify(loanDAO).addLoan(userEmail, bookId);
        assertEquals(2, book.getStock());
    }
}