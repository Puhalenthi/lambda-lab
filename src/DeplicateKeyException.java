package src;

public class DuplicateKeyException extends Exception {
    public DuplicateKeyException(String key){
        super(key + "already exists in memory");
    }
}
