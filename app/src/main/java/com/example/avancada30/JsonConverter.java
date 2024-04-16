package com.example.avancada30;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonConverter {
    private static ObjectMapper objectMapper = new ObjectMapper();

    // Método para converter um objeto para JSON com criptografia dos valores dos atributos
    public static String objectToJsonEncrypted(Object obj) {
        try {
            // Convertendo o objeto para JSON
            String json = objectMapper.writeValueAsString(obj);

            // Convertendo o JSON para um objeto JSON
            JSONObject jsonObject = new JSONObject(json);

            // Iterando sobre as chaves do objeto JSON
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                // Verificando se o valor associado à chave é um objeto JSON
                if (jsonObject.get(key) instanceof JSONObject) {
                    // Se for um objeto JSON, iteramos sobre as suas chaves
                    JSONObject innerObject = jsonObject.getJSONObject(key);
                    Iterator<String> innerKeys = innerObject.keys();
                    while (innerKeys.hasNext()) {
                        String innerKey = innerKeys.next();
                        // Criptografando o valor associado à chave do objeto interno
                        String encryptedValue = CriptografiaAES.criptografar(innerObject.get(innerKey).toString());
                        // Substituindo o valor original pelo valor criptografado
                        innerObject.put(innerKey, encryptedValue);
                    }
                } else {
                    // Criptografando o valor associado à chave
                    String encryptedValue = CriptografiaAES.criptografar(jsonObject.get(key).toString());
                    // Substituindo o valor original pelo valor criptografado
                    jsonObject.put(key, encryptedValue);
                }
            }

            // Convertendo o objeto JSON modificado de volta para uma string JSON
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para converter JSON criptografado de volta para um objeto
    public static <T> T jsonToObjectDecrypted(String encryptedJson, Class<T> clazz) {
        try {
            // Descriptografando o JSON
            String json = CriptografiaAES.descriptografar(encryptedJson);

            // Convertendo o JSON de volta para o objeto
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
