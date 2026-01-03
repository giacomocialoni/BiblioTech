package dao.csv;

import dao.AccountDAO;
import exception.DAOException;
import exception.EmailAlreadyRegisteredException;
import exception.RecordNotFoundException;
import model.Account;
import model.Admin;
import model.User;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVAccountDAO implements AccountDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/users.csv";
    
    @Override
    public Account login(String email, String password) throws DAOException, RecordNotFoundException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",", -1);
                
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
            throw new DAOException("Errore durante il login", e);
        }
        
        throw new RecordNotFoundException("Credenziali non valide");
    }
    
    @Override
    public boolean register(String email, String password, 
                           String firstName, String lastName) 
            throws DAOException, EmailAlreadyRegisteredException {
        
        // Verifica se l'email esiste già
        if (emailExists(email)) {
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
        
        return true;
    }
    
    private boolean emailExists(String email) throws DAOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String[] fields = line.split(",", -1);
                if (fields.length >= 1 && fields[0].equalsIgnoreCase(email)) {
                    return true;
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la verifica dell'email", e);
        }
        
        return false;
    }
    
    private List<String[]> readAllUsers() throws DAOException {
        List<String[]> users = new ArrayList<>();
        
        if (!Files.exists(Paths.get(FILE_PATH))) {
            return users;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                users.add(line.split(",", -1));
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura degli utenti", e);
        }
        
        return users;
    }
    
    private void saveAllUsers(List<String[]> users) throws DAOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            writer.write("email,password,first_name,last_name,role");
            writer.newLine();
            
            for (String[] user : users) {
                writer.write(String.join(",", user));
                writer.newLine();
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il salvataggio degli utenti", e);
        }
    }
}