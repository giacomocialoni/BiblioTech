package controller.app;

import org.junit.jupiter.api.Test;
import utils.LoanResult;

import static org.junit.jupiter.api.Assertions.*;

class SimpleLoanControllerTest {
    
    @Test
    void testLoanResultEnum() {
        // Test semplice sul enum senza dipendenze
        assertEquals("SUCCESS", LoanResult.SUCCESS.name());
        assertEquals("ERROR", LoanResult.ERROR.name());
        assertEquals("MAX_LOANS_REACHED", LoanResult.MAX_LOANS_REACHED.name());
        assertEquals("EXPIRED_LOAN_EXISTS", LoanResult.EXPIRED_LOAN_EXISTS.name());
        assertEquals("INSUFFICIENT_STOCK", LoanResult.INSUFFICIENT_STOCK.name());
        assertEquals("NOT_LOGGED", LoanResult.NOT_LOGGED.name());
        assertEquals("UNAUTHORIZED", LoanResult.UNAUTHORIZED.name());
    }
    
    @Test
    void testConstants() {
        // Test su costanti senza dipendenze
        assertTrue(utils.Constants.MAX_ACTIVE_LOANS > 0);
        assertTrue(utils.Constants.LOANING_DAYS > 0);
    }
}