package ca.jordanfulawka.taskrail.handlers;

import ca.jordanfulawka.taskrail.exceptions.InvalidPayloadException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;


public final class JsonValidator {

//    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonValidator() {}

    private static final JsonMapper jsonBuilder = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
            .build();


    public static <T> T parse(JsonNode jsonNode, Class<T> targetClass) {
        try {
            return jsonBuilder.treeToValue(jsonNode, targetClass);
        } catch (JacksonException e) {
            throw new InvalidPayloadException(e.getMessage());
        }
    }
}