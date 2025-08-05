package org.myblog.users.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

@Component
public class IntegrationTestsUtil {
    @Autowired
    private ObjectMapper objectMapper;

    public Matcher<String> equalToJSON(String json) {
        Matcher<String> result;
        try {
            JsonNode node = objectMapper.readTree(json);
            result = equalTo(objectMapper.writeValueAsString(node));
        } catch (IOException ex) {
            throw new RuntimeException("JSON is broken");
        }

        return result;
    }
}
