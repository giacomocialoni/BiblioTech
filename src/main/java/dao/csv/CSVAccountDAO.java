package dao.csv;

import dao.AccountDAO;
import exception.DAOException;
import exception.EmailAlreadyRegisteredException;
import exception.RecordNotFoundException;
import model.Account;
import model.Admin;
import model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVAccountDAO implements AccountDAO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVAccountDAO.class);
    private static final String FILE_PATH = "src/main/resources/data/users.csv";
    
    @Override
    public Account login(String email, String password) throws DAOException, RecordNotFoundException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            LOGGER.warn("File utenti non trovato: {}", FILE_PATH);
            throw new RecordNotFoundException("Credenziali non valide");
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
        	String header = reader.readLine(); // Leggi e memorizza l'header
            if (header == null) {
                LOGGER.warn("File utenti vuoto");
                throw new RecordNotFoundException("Credenziali non valide");
            }
            LOGGER.debug("Header file utenti: {}", header);
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                
                if (fields.length >= 5 && 
                    fields[0].equals(email) && 
                    fields[1].equals(password)) {
                    
                    String role = fields[4].toLowerCase();
                    if ("admin".equals(role)) {
                        return new Admin(fields[0], fields[1], fields[2], fields[3]);
                    } else {
                        return new User(fields[0], fields[1], fields[2], fields[3]);
                    }
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il login per email: {}", email, e);
            throw new DAOException("Errore durante il login", e);
        }
        
        LOGGER.warn("Login fallito per email: {}", email);
        throw new RecordNotFoundException("Credenziali non valide");
    }
    
    @Override
    public boolean register(String email, String password, 
                           String firstName, String lastName) 
            throws DAOException, EmailAlreadyRegisteredException {
        
        // Verifica se l'email esiste già
        if (emailExists(email)) {
            LOGGER.warn("Tentativo di registrazione con email già esistente: {}", email);
            throw new EmailAlreadyRegisteredException("Email già registrata: " + email);
        }
        
        // Leggi tutti gli utenti
        List<String[]> users = readAllUsers();
        
        // Aggiungi nuovo utente
        users.add(new String[]{
            email, password, firstName, lastName, "logged_user"
        });
        
        // Salva
        saveAllUsers(users);
        
        LOGGER.info("Utente registrato con successo: {}", email);
        return true;
    }
    
    private boolean emailExists(String email) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            return false;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = parseCSVLine(line);
                if (fields.length >= 1 && fields[0].equalsIgnoreCase(email)) {
                    return true;
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la verifica dell'email: {}", email, e);
            throw new DAOException("Errore durante la verifica dell'email", e);
        }
        
        return false;
    }
    
    private List<String[]> readAllUsers() throws DAOException {
        List<String[]> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return users;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                users.add(parseCSVLine(line));
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la lettura degli utenti", e);
            throw new DAOException("Errore durante la lettura degli utenti", e);
        }
        
        return users;
    }
    
    private void saveAllUsers(List<String[]> users) throws DAOException {
        try {
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(FILE_PATH), 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                
                writer.write("email,password,first_name,last_name,role");
                writer.newLine();
                
                for (String[] user : users) {
                    writer.write(formatCSVLine(user));
                    writer.newLine();
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il salvataggio degli utenti", e);
            throw new DAOException("Errore durante il salvataggio degli utenti", e);
        }
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
    
    private String formatCSVLine(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(",");
            String field = fields[i] != null ? fields[i] : "";
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                sb.append("\"").append(field.replace("\"", "\"\"")).append("\"");
            } else {
                sb.append(field);
            }
        }
        return sb.toString();
    }
}