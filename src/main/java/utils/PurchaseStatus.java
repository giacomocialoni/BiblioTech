package utils;

public enum PurchaseStatus {
    RESERVED,
    PURCHASED;
    
    public static PurchaseStatus fromString(String status) {
        if (status == null) return null;
        
        switch (status.toUpperCase()) {
            case "RESERVED":
                return RESERVED;
            case "PURCHASED":
                return PURCHASED;
            default:
                return null;
        }
    }
    
    public static String toString(PurchaseStatus status) {
        if (status == null) return null;
        return status.name();
    }
}