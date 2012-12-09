package no.koredu.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.*;
import com.google.common.collect.Lists;
import no.koredu.common.Sanitizable;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class GCMObjectPusher implements ObjectPusher {

  private static final Logger log = Logger.getLogger(GCMObjectPusher.class.getName());
  private static final String GCM_API_KEY = "AIzaSyBHPdIvAavNekQATYDdcKzrauH263BD9vc";

  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Override
  public void pushSmsCommand(String phoneNumber, String smsMessage, String... deviceIds) {
    List<String> deviceIdList = Lists.newArrayList(deviceIds);
    log.info("Sending " + phoneNumber + ":" + smsMessage + " to " + deviceIdList);
    Sender sender = new Sender(GCM_API_KEY);
    Message message = new Message.Builder()
        .addData("action", "SEND_SMS")
        .addData("message", smsMessage)
        .addData("phoneNumber", phoneNumber)
        .timeToLive(0)
        .delayWhileIdle(false)
        .build();
    try {
      MulticastResult multicastResult = sender.send(message, deviceIdList, 5);
      for (Result result : multicastResult.getResults()) {
        if (result != null) {
          String canonicalRegId = result.getCanonicalRegistrationId();
          if (canonicalRegId != null) {
            // same device has more than one registration ID: update database
            log.warning("same device has more than one registration ID: TODO: update database. " +
                "Got canonicalRegId=" + canonicalRegId + " for result=" + result);
          }
        } else {
          String error = result.getErrorCodeName();
          log.warning("Got error=" + error + " for result=" + result);
          if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
            // application has been removed from device - unregister database
            log.info("application has been removed from device - TODO: unregister in database");
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to ask " + deviceIdList + " to send SMS " + smsMessage + " to " + phoneNumber,
          e);
    }
  }

  @Override
  public void pushObject(String action, Sanitizable object, String... deviceIds) {
    List<String> deviceIdList = Lists.newArrayList(deviceIds);
    log.info("Sending " + action + " to " + deviceIdList);
    object.sanitize();
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