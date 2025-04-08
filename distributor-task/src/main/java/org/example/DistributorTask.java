package org.example;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DistributorTask {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @End
    public static String calculateEndRange(String jsonData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonData);
            JsonNode passwordLength = jsonNode.path("passwordLength");
            BigInteger asciiCharacters = BigInteger.valueOf(94);
            return asciiCharacters.pow(passwordLength.asInt()).toString();
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Process
    public static boolean processResult(String result) {
        try {
            JsonNode jsonNode = objectMapper.readTree(result);
            JsonNode passwordNode = jsonNode.path("password");

            if (passwordNode != null && !passwordNode.isNull()) {
                String password = passwordNode.asText();

                Path passwordFile = Paths.get("password.txt");
                Files.writeString(passwordFile, password, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            return false;
        }
    }

}
