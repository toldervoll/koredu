package no.koredu.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;

import java.io.IOException;

public class GCMObjectPusher implements ObjectPusher {

  private static final String GCM_API_KEY = "AIzaSyCj5w_38CI0Sj03VEx_2-3q03Ej18gx5SM";

  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  public void pushSmsCommand(String phoneNumber, String smsMessage, String deviceId) {
    Sender sender = new Sender(GCM_API_KEY);
    Message message = new Message.Builder()
        .addData("action", "SEND_SMS")
        .addData("message", smsMessage)
        .addData("phoneNumber", phoneNumber)
        .build();
    try {
      sender.send(message, deviceId, 5);
    } catch (IOException e) {
      throw new RuntimeException("Failed to ask " + deviceId + " to send SMS " + smsMessage + " to " + phoneNumber, e);
    }
  }

  @Override
  public <T> void pushObject(String action, T object, String deviceId) {
    Sender sender = new Sender(GCM_API_KEY);
    String json;
    try {
      json = jsonMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert object + " + object + " to JSON", e);
    }
    Message message = new Message.Builder()
        .addData("action", action)
        .addData("data", json)
        .build();
    try {
      sender.send(message, deviceId, 5);
    } catch (IOException e) {
      throw new RuntimeException("Failed to push object " + object + " to " + deviceId, e);
    }
  }
}