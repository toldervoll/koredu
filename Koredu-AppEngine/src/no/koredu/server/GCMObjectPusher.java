package no.koredu.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

public class GCMObjectPusher implements ObjectPusher {

  private static final String GCM_API_KEY = "AIzaSyCj5w_38CI0Sj03VEx_2-3q03Ej18gx5SM";

  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  public void pushSmsCommand(String phoneNumber, String smsMessage, String... deviceIds) {
    List<String> deviceIdList = Lists.newArrayList(deviceIds);
    Sender sender = new Sender(GCM_API_KEY);
    Message message = new Message.Builder()
        .addData("action", "SEND_SMS")
        .addData("message", smsMessage)
        .addData("phoneNumber", phoneNumber)
        .build();
    try {
      sender.send(message, deviceIdList, 5);
    } catch (IOException e) {
      throw new RuntimeException("Failed to ask " + deviceIdList + " to send SMS " + smsMessage + " to " + phoneNumber,
          e);
    }
  }

  @Override
  public <T> void pushObject(String action, T object, String... deviceIds) {
    List<String> deviceIdList = Lists.newArrayList(deviceIds);
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
      sender.send(message, deviceIdList, 5);
    } catch (IOException e) {
      throw new RuntimeException("Failed to push object " + object + " to " + deviceIdList, e);
    }
  }
}