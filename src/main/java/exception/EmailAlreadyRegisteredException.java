package exception;

@SuppressWarnings("serial")
public class EmailAlreadyRegisteredException extends Exception {
    
    private final String email;
    
    public EmailAlreadyRegisteredException(String email) {
        super("Email '" + email + "' è già registrata nel sistema");
        this.email = email;
    }
    
    public EmailAlreadyRegisteredException(String email, String customMessage) {
        super(customMessage);
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getUserFriendlyMessage() {
        return "Questa email è già registrata!";
    }
}