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
    
    @Override
    public User getUser(String email) throws DAOException, RecordNotFoundException {
    	Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            LOGGER.warn("File utenti non trovato durante ricerca per email: {}", email);
            throw new RecordNotFoundException("Utente non trovato con email: " + email);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine(); // Leggi e memorizza
            if (header == null) {
                LOGGER.warn("File utenti vuoto");
                throw new RecordNotFoundException("Utente non trovato con email: " + email);
            }
            LOGGER.debug("Header file utenti: {}", header);
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 5 && fields[0].equalsIgnoreCase(email)) {
                    LOGGER.debug("Utente trovato: {}", email);
                    return new User(fields[0], fields[1], fields[2], fields[3]);
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la ricerca dell'utente: {}", email, e);
            throw new DAOException("Errore durante la ricerca dell'utente", e);
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
            String header = reader.readLine(); // Memorizza l'header
            if (header == null) {
                LOGGER.debug("File utenti vuoto durante ricerca");
                return users;
            }
            LOGGER.trace("Header durante ricerca: {}", header);
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 5 && "logged_user".equals(fields[4])) {
                    String email = fields[0];
                    String firstName = fields[2];
                    String lastName = fields[3];
                    
                    if (email.toLowerCase().contains(lowerSearch) ||
                        firstName.toLowerCase().contains(lowerSearch) ||
                        lastName.toLowerCase().contains(lowerSearch)) {
                        
                        users.add(new User(email, fields[1], firstName, lastName));
                    }
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la ricerca degli utenti per termine: {}", searchTerm, e);
            throw new DAOException("Errore durante la ricerca degli utenti", e);
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
            throw new DAOException("File utenti non trovato");
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            lines.add(reader.readLine()); // Keep header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 5 && fields[0].equalsIgnoreCase(email)) {
                    found = true;
                    LOGGER.debug("Utente {} trovato per eliminazione", email);
                    continue; // Skip this user
                }
                lines.add(line);
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la lettura del file utenti per eliminazione: {}", email, e);
            throw new DAOException("Errore durante la cancellazione dell'utente", e);
        }
        
        if (!found) {
            LOGGER.warn("Utente non trovato per la cancellazione: {}", email);
            throw new DAOException("Utente non trovato per la cancellazione: " + email);
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            
            LOGGER.info("Utente eliminato: {}", email);
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la scrittura del file utenti dopo eliminazione: {}", email, e);
            throw new DAOException("Errore durante la cancellazione dell'utente", e);
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
            String header = reader.readLine(); // Memorizza l'header
            if (header == null) {
                LOGGER.debug("File utenti vuoto durante recupero per ruolo");
                return users;
            }
            LOGGER.trace("Header durante recupero per ruolo: {}", header);
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 5) {
                    if (roleFilter == null || roleFilter.equals(fields[4])) {
                        users.add(new User(fields[0], fields[1], fields[2], fields[3]));
                    }
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la lettura del file utenti", e);
            throw new DAOException("Errore durante il recupero degli utenti", e);
        }
        
        LOGGER.debug("Recuperati {} utenti", users.size());
        return users;
    }
    
    private String[] parseCSVLine(String line) {
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
        return fields.toArray(new String[0]);
    }
}