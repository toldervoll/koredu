package no.koredu.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {

  private final ObjectMapper objectMapper;

  public JsonMapper() {
    objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
  }

  public String toJson(Object object) {
    String json;
    try {
      json = objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException("Failed to JSON serialize " + object, e);
    }
    return json;
  }

  public <T> T fromJson(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json,clazz);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create " + clazz.getName() + " from JSON=" + json);
    }
  }


}
