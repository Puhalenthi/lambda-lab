package src;

import java.util.HashMap;

public class Memory {
    private static HashMap<String, Expression> memory = new HashMap<>();

    public static Expression add(String variableName, Expression expression) throws DuplicateKeyException {
        if (memory.containsKey(variableName)) {
            throw new DuplicateKeyException(variableName + " is already defined.");
        }
        memory.put(variableName, expression);
        return expression;
    }

    public static Expression get(String variableName) throws IllegalArgumentException {
        return memory.get(variableName);
    }

    public static boolean contains(String variableName) {
        return memory.containsKey(variableName);
    }
}
