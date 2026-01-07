package dao.csv;

import dao.WishlistDAO;
import model.User;
import model.Wishlist;
import exception.DAOException;
import exception.RecordNotFoundException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVWishlistDAO implements WishlistDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/wishlist.csv";
    private static final String CSV_HEADER = "user_email,book_id";
    
    @Override
    public void addToWishlist(String userEmail, int bookId) throws DAOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            
            if (!Files.exists(Paths.get(FILE_PATH)) || Files.size(Paths.get(FILE_PATH)) == 0) {
                writer.write(CSV_HEADER);
                writer.newLine();
            }
            
            String line = userEmail + "," + bookId;
            writer.write(line);
            writer.newLine();
            
        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiunta alla wishlist per utente " + userEmail, e);
        }
    }
    
    @Override
    public boolean isInWishlist(String userEmail, int bookId) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            return false;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            // Verifica header
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV wishlist non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",");
                if (fields[0].equals(userEmail) && Integer.parseInt(fields[1]) == bookId) {
                    return true;
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il controllo della wishlist per utente " + userEmail, e);
        }
        
        return false;
    }
    
    @Override
    public void removeFromWishlist(String userEmail, int bookId) throws DAOException {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            lines.add(reader.readLine()); // Keep header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",");
                if (fields[0].equals(userEmail) && Integer.parseInt(fields[1]) == bookId) {
                    found = true;
                } else {
                    lines.add(line);
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la rimozione dalla wishlist per utente " + userEmail, e);
        }
        
        if (!found) {
            return;
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la scrittura del file wishlist dopo rimozione", e);
        }
    }
    
    @Override
    public List<Wishlist> getWishlistByUser(String userEmail) throws DAOException, RecordNotFoundException {
        List<Wishlist> wishlist = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
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
            throw new DAOException("Errore durante il recupero della wishlist per utente " + userEmail, e);
        }
        
        return wishlist;
    }
    
    @Override
    public List<User> getUsersWithBookInWishlist(int bookId) throws DAOException {
        List<User> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return users;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",");
                if (Integer.parseInt(fields[1]) == bookId) {
                    users.add(new User(fields[0], "", "", ""));
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il recupero degli utenti con libro " + bookId + " in wishlist", e);
        }
        
        return users;
    }
}