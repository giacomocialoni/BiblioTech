package bean;

import org.junit.jupiter.api.Test;
import utils.LoanStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanBeanTest {
    
    @Test
    void testLoanBeanCreation() {
        LoanBean bean = new LoanBean();
        
        // Test setter/getter
        bean.setId(1);
        bean.setUserEmail("test@example.com");
        bean.setStatus(LoanStatus.RESERVED);
        bean.setReservedDate(LocalDate.now());
        
        assertEquals(1, bean.getId());
        assertEquals("test@example.com", bean.getUserEmail());
        assertEquals(LoanStatus.RESERVED, bean.getStatus());
        assertNotNull(bean.getReservedDate());
    }
    
    @Test
    void testLoanBeanIsReturned() {
        LoanBean bean = new LoanBean();
        
        bean.setStatus(LoanStatus.RETURNED);
        assertTrue(bean.isReturned());
        
        bean.setStatus(LoanStatus.LOANED);
        assertFalse(bean.isReturned());
    }
    
    @Test
    void testLoanBeanIsExpired() {
        LoanBean bean = new LoanBean();
        
        // Test con status EXPIRED
        bean.setStatus(LoanStatus.EXPIRED);
        assertTrue(bean.isExpired());
        
        // Test con data passata
        bean.setStatus(LoanStatus.LOANED);
        bean.setReturningDate(LocalDate.now().minusDays(1));
        assertTrue(bean.isExpired());
        
        // Test con data futura
        bean.setReturningDate(LocalDate.now().plusDays(1));
        assertFalse(bean.isExpired());
    }
}