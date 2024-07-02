package demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    public String key;
    public Map<String, String> attributes;

    // Default constructor
    public Event() {}

    // Constructor
    public Event(String key, Map<String, String> attributes) {
        this.key = key;
        this.attributes = attributes;
    }
}

