package com.example.avancada30;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonConverter {
    private static ObjectMapper objectMapper = new ObjectMapper();

    // Método para converter um objeto para JSON
    public static String objectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para converter JSON de volta para um objeto
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para converter um atributo de um objeto para uma string JSON
    public static String attributeToJson(Object attribute) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("attribute", attribute);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }

    // Método para converter uma string JSON em um atributo de um objeto
    public static Object jsonToAttribute(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            return jsonObject.get("attribute");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // Exemplo de uso
        String jsonString = attributeToJson(42); // Substitua 42 pelo seu atributo
        System.out.println("Atributo em formato JSON: " + jsonString);

        Object attribute = jsonToAttribute(jsonString);
        System.out.println("Atributo recuperado: " + attribute);
    }
}