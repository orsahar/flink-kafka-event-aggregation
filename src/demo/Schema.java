package demo;


import java.io.Serializable;
import java.util.Set;

public class Schema implements Serializable {
    private static final long serialVersionUID = 1L; // recommended for Serializable classes
    private Set<String> keys;
    public Set<String> requiredKeys;

    // Constructor

    public Schema(Set<String> keys) {
        this.keys = keys;
        this.requiredKeys = requiredKeys;

    }

    // Getters and Setters
    public Set<String> getKeys() {
        return keys;
    }

    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }
}