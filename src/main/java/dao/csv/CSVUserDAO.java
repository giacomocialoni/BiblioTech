package dao.csv;

import dao.UserDAO;
import model.User;
import exception.DAOException;
import exception.RecordNotFoundException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVUserDAO implements UserDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/users.csv";
    
    @Override
    public User getUser(String email) throws DAOException, RecordNotFoundException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 5 && fields[0].equalsIgnoreCase(email)) {
                    return new User(fields[0], fields[1], fields[2], fields[3]);
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura del file CSV", e);
        }
        
        throw new RecordNotFoundException("Utente non trovato con email: " + email);
    }
    
    @Override
    public List<User> getAllUsers() throws DAOException {
        return getUsersByRole(null);
    }
    
    @Override
    public List<User> searchUsers(String searchTerm) throws DAOException, RecordNotFoundException {
        List<User> users = new ArrayList<>();
        String lowerSearch = searchTerm.toLowerCase();
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
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
            throw new DAOException("Errore durante la ricerca degli utenti", e);
        }
        
        if (users.isEmpty()) {
            throw new RecordNotFoundException("Nessun utente trovato per: " + searchTerm);
        }
        
        return users;
    }
    
    @Override
    public void deleteUser(String email) throws DAOException, RecordNotFoundException {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            lines.add(reader.readLine()); // Keep header
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 5 && fields[0].equalsIgnoreCase(email)) {
                    found = true;
                    continue; // Skip this user
                }
                lines.add(line);
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura del file CSV", e);
        }
        
        if (!found) {
            throw new RecordNotFoundException("Utente non trovato per la cancellazione: " + email);
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la scrittura del file CSV", e);
        }
    }
    
    private List<User> getUsersByRole(String roleFilter) throws DAOException {
        List<User> users = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 5) {
                    if (roleFilter == null || roleFilter.equals(fields[4])) {
                        users.add(new User(fields[0], fields[1], fields[2], fields[3]));
                    }
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura del file CSV", e);
        }
        
        return users;
    }
}