package dao.memory;

import dao.WishlistDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.User;
import model.Wishlist;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryWishlistDAO implements WishlistDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryWishlistDAO.class);
    
    // Struttura dati per memorizzare le wishlist
    // Map<userEmail, List<bookId>>
    private final Map<String, List<Integer>> wishlistData;
    
    // Riferimento a UserDAO per recuperare i dettagli utente (se necessario)
    // private final UserDAO userDAO;
    
    public InMemoryWishlistDAO() {
        this.wishlistData = new ConcurrentHashMap<>();
        LOGGER.info("InMemoryWishlistDAO inizializzato - storage in memoria");
    }
    
    /* Costruttore alternativo se hai bisogno del riferimento a UserDAO
    public InMemoryWishlistDAO(UserDAO userDAO) {
        this.wishlistData = new ConcurrentHashMap<>();
        this.userDAO = userDAO;
        LOGGER.info("InMemoryWishlistDAO inizializzato con UserDAO");
    }
    */

    @Override
    public void addToWishlist(String userEmail, int bookId) throws DAOException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            String errorMessage = "Email utente non valida per l'aggiunta alla wishlist";
            LOGGER.error(errorMessage);
            throw new DAOException(errorMessage);
        }
        
        if (bookId <= 0) {
            String errorMessage = "ID libro non valido per l'aggiunta alla wishlist: " + bookId;
            LOGGER.error(errorMessage);
            throw new DAOException(errorMessage);
        }
        
        try {
            // Sincronizziamo l'accesso alla mappa
            synchronized (wishlistData) {
                List<Integer> userWishlist = wishlistData.computeIfAbsent(userEmail, k -> new CopyOnWriteArrayList<>());
                
                // Controllo se il libro è già nella wishlist
                if (userWishlist.contains(bookId)) {
                    LOGGER.warn("Libro ID {} già presente nella wishlist dell'utente {}", bookId, userEmail);
                    return; // Non fare nulla se già presente
                }
                
                userWishlist.add(bookId);
                LOGGER.info("Libro ID {} aggiunto alla wishlist dell'utente {}", bookId, userEmail);
            }
            
        } catch (Exception e) {
            String errorMessage = String.format("Errore durante l'aggiunta del libro %d alla wishlist dell'utente %s", 
                                                bookId, userEmail);
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public void removeFromWishlist(String userEmail, int bookId) throws DAOException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            LOGGER.warn("Email utente non valida per la rimozione dalla wishlist");
            return;
        }
        
        try {
            boolean removed = false;
            
            synchronized (wishlistData) {
                List<Integer> userWishlist = wishlistData.get(userEmail);
                if (userWishlist != null) {
                    removed = userWishlist.remove(Integer.valueOf(bookId));
                    
                    // Se la wishlist dell'utente è vuota, rimuoviamo l'entry dalla mappa
                    if (userWishlist.isEmpty()) {
                        wishlistData.remove(userEmail);
                    }
                }
            }
            
            if (removed) {
                LOGGER.info("Libro ID {} rimosso dalla wishlist dell'utente {}", bookId, userEmail);
            } else {
                LOGGER.debug("Libro ID {} non trovato nella wishlist dell'utente {} (rimozione ignorata)", 
                            bookId, userEmail);
            }
            
        } catch (Exception e) {
            String errorMessage = String.format("Errore durante la rimozione del libro %d dalla wishlist dell'utente %s", 
                                                bookId, userEmail);
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public boolean isInWishlist(String userEmail, int bookId) throws DAOException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            LOGGER.debug("Email utente non valida per il controllo della wishlist");
            return false;
        }
        
        try {
            synchronized (wishlistData) {
                List<Integer> userWishlist = wishlistData.get(userEmail);
                boolean isPresent = userWishlist != null && userWishlist.contains(bookId);
                
                LOGGER.debug("Controllo wishlist - Utente {} ha libro {}: {}", 
                            userEmail, bookId, isPresent);
                return isPresent;
            }
            
        } catch (Exception e) {
            String errorMessage = String.format("Errore durante il controllo del libro %d nella wishlist dell'utente %s", 
                                                bookId, userEmail);
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Wishlist> getWishlistByUser(String userEmail) throws DAOException, RecordNotFoundException {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            String errorMessage = "Email utente non valida per il recupero della wishlist";
            LOGGER.error(errorMessage);
            throw new DAOException(errorMessage);
        }
        
        try {
            List<Wishlist> wishlistItems = new ArrayList<>();
            
            synchronized (wishlistData) {
                List<Integer> userWishlist = wishlistData.get(userEmail);
                if (userWishlist == null || userWishlist.isEmpty()) {
                    LOGGER.debug("Wishlist vuota per l'utente {}", userEmail);
                    return wishlistItems; // Restituisce lista vuota
                }
                
                // Convertiamo gli ID libro in oggetti Wishlist
                for (Integer bookId : userWishlist) {
                    wishlistItems.add(new Wishlist(userEmail, bookId));
                }
            }
            
            LOGGER.debug("Recuperata wishlist per utente {}: {} elementi", userEmail, wishlistItems.size());
            return wishlistItems;
            
        } catch (Exception e) {
            String errorMessage = "Errore durante il recupero della wishlist per l'utente " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<User> getUsersWithBookInWishlist(int bookId) throws DAOException {
        if (bookId <= 0) {
            LOGGER.warn("ID libro non valido per la ricerca utenti nella wishlist: {}", bookId);
            return new ArrayList<>();
        }
        
        try {
            List<User> users = new ArrayList<>();
            
            synchronized (wishlistData) {
                // Iteriamo su tutte le entry della mappa per trovare gli utenti che hanno il libro
                for (Map.Entry<String, List<Integer>> entry : wishlistData.entrySet()) {
                    String userEmail = entry.getKey();
                    List<Integer> userWishlist = entry.getValue();
                    
                    if (userWishlist != null && userWishlist.contains(bookId)) {
                        // Creiamo un utente parziale (senza password e altri dettagli)
                        // Nota: in un'implementazione reale, dovremmo recuperare i dettagli completi dal UserDAO
                        User user = new User(userEmail, "", "", "");
                        users.add(user);
                    }
                }
            }
            
            LOGGER.debug("Trovati {} utenti con il libro ID {} nella wishlist", users.size(), bookId);
            return users;
            
        } catch (Exception e) {
            String errorMessage = "Errore durante il recupero degli utenti con il libro " + bookId + " nella wishlist";
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }
    
    /* METODI UTILITY AGGIUNTIVI (non richiesti dall'interfaccia ma utili) */
    
    /**
     * Metodo per ottenere tutte le wishlist (per debugging o amministrazione)
     */
    public Map<String, List<Integer>> getAllWishlists() {
        synchronized (wishlistData) {
            // Restituiamo una copia difensiva
            Map<String, List<Integer>> copy = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : wishlistData.entrySet()) {
                copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return copy;
        }
    }
    
    /**
     * Metodo per pulire tutte le wishlist (utile per testing)
     */
    public void clearAllWishlists() {
        synchronized (wishlistData) {
            int count = wishlistData.size();
            wishlistData.clear();
            LOGGER.info("Pulite tutte le wishlist ({} utenti)", count);
        }
    }
    
    /**
     * Metodo per ottenere il numero totale di elementi nelle wishlist
     */
    public int getTotalWishlistItems() {
        synchronized (wishlistData) {
            int total = 0;
            for (List<Integer> wishlist : wishlistData.values()) {
                total += wishlist.size();
            }
            return total;
        }
    }
    
    /**
     * Metodo per ottenere il conteggio per utente
     */
    public int getWishlistCountByUser(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            return 0;
        }
        
        synchronized (wishlistData) {
            List<Integer> userWishlist = wishlistData.get(userEmail);
            return userWishlist != null ? userWishlist.size() : 0;
        }
    }
}