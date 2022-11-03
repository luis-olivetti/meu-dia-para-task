package org.example;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.swing.*;
import java.io.FileReader;
import java.io.StringWriter;

public class ConfigFacade {
    public Config getConfiguration(String path) {
        return getConfigByFile(path);
    }

    private Config getConfigByFile(String path) {
        try (FileReader reader = new FileReader(path))
        {
            JsonReader jsonReader = Json.createReader(reader);
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = Json.createWriter(stringWriter);
            jsonWriter.writeObject(jsonReader.readObject());

            String json = stringWriter.toString();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            return mapper.readValue(jsonNode, Config.class);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),"Dialog", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
