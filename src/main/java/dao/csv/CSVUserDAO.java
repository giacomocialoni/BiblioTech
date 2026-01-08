package dao.csv;

import dao.UserDAO;
import model.User;
import exception.DAOException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVUserDAO implements UserDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/users.csv";
    private static final String CSV_HEADER = "email,password,first_name,last_name,role";
    
    @Override
    public User getUser(String email) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            throw new DAOException("Utente non trovato con email: " + email);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV utenti non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= 5 && fields.get(0).equalsIgnoreCase(email)) {
                    return new User(fields.get(0), fields.get(1), fields.get(2), fields.get(3));
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la ricerca dell'utente " + email, e);
        }
        
        throw new DAOException("Utente non trovato con email: " + email);
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
            return users;
        }
        
        String lowerSearch = searchTerm.toLowerCase();
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV utenti non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= 5 && "logged_user".equals(fields.get(4))) {
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
            throw new DAOException("Errore durante la ricerca degli utenti per termine " + searchTerm, e);
        }
        
        return users;
    }
    
    @Override
    public void deleteUser(String email) throws DAOException {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            throw new DAOException("Utente non trovato: " + email);
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            lines.add(reader.readLine()); // Keep header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= 5 && fields.get(0).equalsIgnoreCase(email)) {
                    found = true;
                } else {
                    lines.add(line);
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura del file utenti per eliminazione di " + email, e);
        }
        
        if (!found) {
            throw new DAOException("Utente non trovato per la cancellazione: " + email);
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la scrittura del file utenti dopo eliminazione di " + email, e);
        }
    }
    
    private List<User> getUsersByRole(String roleFilter) throws DAOException {
        List<User> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return users;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV utenti non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                List<String> fields = parseCSVLine(line);
                if (fields.size() >= 5 && (roleFilter == null || roleFilter.equals(fields.get(4)))) {
                	users.add(new User(fields.get(0), fields.get(1), fields.get(2), fields.get(3)));
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il recupero degli utenti dal file", e);
        }
        
        return users;
    }
    
    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i += 2;
                } else {
                    inQuotes = !inQuotes;
                    i++;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
                i++;
            } else {
                currentField.append(c);
                i++;
            }
        }

        fields.add(currentField.toString());
        return fields;
    }
}