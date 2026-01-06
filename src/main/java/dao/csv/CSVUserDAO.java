package dao.csv;

import dao.UserDAO;
import model.User;
import exception.DAOException;
import exception.RecordNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVUserDAO implements UserDAO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVUserDAO.class);
    private static final String FILE_PATH = "src/main/resources/data/users.csv";
    private static final String[] COLUMNS = {"email", "password", "first_name", "last_name", "role"};
    
    @Override
    public User getUser(String email) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            LOGGER.warn("File utenti non trovato durante ricerca per email: {}", email);
            throw new RecordNotFoundException("Utente non trovato con email: " + email);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null) {
                LOGGER.warn("File utenti vuoto");
                throw new RecordNotFoundException("Utente non trovato con email: " + email);
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= COLUMNS.length && fields.get(0).equalsIgnoreCase(email)) {
                    LOGGER.debug("Utente trovato: {}", email);
                    return new User(fields.get(0), fields.get(1), fields.get(2), fields.get(3));
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la ricerca dell'utente: {}", email, e);
            throw new DAOException("Errore durante la ricerca dell'utente " + email, e);
        }
        
        LOGGER.warn("Utente non trovato: {}", email);
        throw new RecordNotFoundException("Utente non trovato con email: " + email);
    }
    
    @Override
    public List<User> getAllUsers() throws DAOException {
        return getUsersByRole("logged_user");
    }
    
    @Override
    public List<User> searchUsers(String searchTerm) throws DAOException {
        List<User> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.warn("File utenti non trovato durante ricerca");
            return users;
        }
        
        String lowerSearch = searchTerm.toLowerCase();
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null) {
                LOGGER.debug("File utenti vuoto durante ricerca");
                return users;
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= COLUMNS.length && "logged_user".equals(fields.get(4))) {
                    String email = fields.get(0);
                    String firstName = fields.get(2);
                    String lastName = fields.get(3);
                    
                    if (email.toLowerCase().contains(lowerSearch) ||
                        firstName.toLowerCase().contains(lowerSearch) ||
                        lastName.toLowerCase().contains(lowerSearch)) {
                        
                        users.add(new User(email, fields.get(1), firstName, lastName));
                    }
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la ricerca degli utenti per termine: {}", searchTerm, e);
            throw new DAOException("Errore durante la ricerca degli utenti per termine " + searchTerm, e);
        }
        
        LOGGER.debug("Ricerca utenti '{}': trovati {} risultati", searchTerm, users.size());
        return users;
    }
    
    @Override
    public void deleteUser(String email) throws DAOException {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.warn("File utenti non trovato durante eliminazione utente: {}", email);
            throw new DAOException("File utenti non trovato durante eliminazione di " + email);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            lines.add(reader.readLine());
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= COLUMNS.length && fields.get(0).equalsIgnoreCase(email)) {
                    found = true;
                    LOGGER.debug("Utente {} trovato per eliminazione", email);
                } else {
                    lines.add(line);
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la lettura del file utenti per eliminazione: {}", email, e);
            throw new DAOException("Errore durante la lettura del file utenti per eliminazione di " + email, e);
        }
        
        if (!found) {
            LOGGER.warn("Utente non trovato per la cancellazione: {}", email);
            throw new RecordNotFoundException("Utente non trovato per la cancellazione: " + email);
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            
            LOGGER.info("Utente eliminato: {}", email);
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la scrittura del file utenti dopo eliminazione: {}", email, e);
            throw new DAOException("Errore durante la scrittura del file utenti dopo eliminazione di " + email, e);
        }
    }
    
    private List<User> getUsersByRole(String roleFilter) throws DAOException {
        List<User> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.warn("File utenti non trovato durante recupero per ruolo");
            return users;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null) {
                LOGGER.debug("File utenti vuoto durante recupero per ruolo");
                return users;
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= COLUMNS.length) {
                    if (roleFilter == null || roleFilter.equals(fields.get(4))) {
                        users.add(new User(fields.get(0), fields.get(1), fields.get(2), fields.get(3)));
                    }
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la lettura del file utenti", e);
            throw new DAOException("Errore durante il recupero degli utenti dal file", e);
        }
        
        LOGGER.debug("Recuperati {} utenti", users.size());
        return users;
    }
    
    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields;
    }
}