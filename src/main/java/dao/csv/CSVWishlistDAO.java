package dao.csv;

import dao.WishlistDAO;
import model.User;
import model.Wishlist;
import exception.DAOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVWishlistDAO implements WishlistDAO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVWishlistDAO.class);
    private static final String FILE_PATH = "src/main/resources/data/wishlist.csv";
    
    @Override
    public void addToWishlist(String userEmail, int bookId) throws DAOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            
            if (!Files.exists(Paths.get(FILE_PATH)) || Files.size(Paths.get(FILE_PATH)) == 0) {
                writer.write("user_email,book_id");
                writer.newLine();
            }
            
            String line = userEmail + "," + bookId;
            writer.write(line);
            writer.newLine();
            
            LOGGER.info("Elemento aggiunto alla wishlist: utente {}, libro {}", userEmail, bookId);
            
        } catch (IOException e) {
            LOGGER.error("Errore durante l'aggiunta alla wishlist per utente {} libro {}", userEmail, bookId, e);
            throw new DAOException("Errore durante l'aggiunta alla wishlist", e);
        }
    }
    
    @Override
    public void removeFromWishlist(String userEmail, int bookId) throws DAOException {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.warn("File wishlist non trovato durante rimozione");
            return;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            lines.add(reader.readLine()); // Keep header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",");
                if (fields[0].equals(userEmail) && Integer.parseInt(fields[1]) == bookId) {
                    found = true;
                    LOGGER.debug("Elemento trovato per rimozione: utente {}, libro {}", userEmail, bookId);
                    continue; // Skip this line (remove)
                }
                lines.add(line);
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la rimozione dalla wishlist per utente {} libro {}", userEmail, bookId, e);
            throw new DAOException("Errore durante la rimozione dalla wishlist", e);
        }
        
        if (!found) {
            LOGGER.debug("Elemento non trovato nella wishlist per rimozione: utente {}, libro {}", userEmail, bookId);
            return; // Non esiste, non fare nulla
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            
            LOGGER.info("Elemento rimosso dalla wishlist: utente {}, libro {}", userEmail, bookId);
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la scrittura del file wishlist dopo rimozione", e);
            throw new DAOException("Errore durante la scrittura del file wishlist", e);
        }
    }
    
    @Override
    public boolean isInWishlist(String userEmail, int bookId) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            LOGGER.debug("File wishlist non trovato, elemento non presente");
            return false;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",");
                if (fields[0].equals(userEmail) && Integer.parseInt(fields[1]) == bookId) {
                    LOGGER.debug("Elemento trovato nella wishlist: utente {}, libro {}", userEmail, bookId);
                    return true;
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il controllo della wishlist per utente {} libro {}", userEmail, bookId, e);
            throw new DAOException("Errore durante il controllo della wishlist", e);
        }
        
        LOGGER.debug("Elemento non trovato nella wishlist: utente {}, libro {}", userEmail, bookId);
        return false;
    }
    
    @Override
    public List<Wishlist> getWishlistByUser(String userEmail) throws DAOException {
        List<Wishlist> wishlist = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.debug("File wishlist non trovato, restituita lista vuota per utente: {}", userEmail);
            return wishlist;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",");
                if (fields[0].equals(userEmail)) {
                    wishlist.add(new Wishlist(fields[0], Integer.parseInt(fields[1])));
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il recupero della wishlist per utente: {}", userEmail, e);
            throw new DAOException("Errore durante il recupero della wishlist", e);
        }
        
        LOGGER.debug("Recuperati {} elementi wishlist per utente {}", wishlist.size(), userEmail);
        return wishlist;
    }
    
    @Override
    public List<User> getUsersWithBookInWishlist(int bookId) throws DAOException {
        List<User> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.debug("File wishlist non trovato, restituita lista vuota per libro: {}", bookId);
            return users;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",");
                if (Integer.parseInt(fields[1]) == bookId) {
                    // Dovremmo avere un riferimento a UserDAO per ottenere i dettagli utente
                    // Per ora restituiamo utenti parziali
                    users.add(new User(fields[0], "", "", ""));
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il recupero degli utenti con libro {} in wishlist", bookId, e);
            throw new DAOException("Errore durante il recupero degli utenti con libro in wishlist", e);
        }
        
        LOGGER.debug("Recuperati {} utenti con libro {} in wishlist", users.size(), bookId);
        return users;
    }
}