package src;

import java.util.HashMap;

public class Memory {
    private static HashMap<String, Expression> memory = new HashMap<>();

    public static Expression addToMemory(String variable, Expression expression) throws DuplicateKeyException{
        if (memory.containsKey(variable)){
            throw new DuplicateKeyException(variable + " is already defined.");
        }
        memory.put(variable, expression);
        return expression;
    }

    public static Expression getFromMemory(String variable) throws IllegalArgumentException{
        if (!memory.containsKey(variable)) {
            throw new IllegalArgumentException(variable + "is not defined.");
        }
        return memory.get(variable);
    }
    
}
