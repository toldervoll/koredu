package no.koredu.testing;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import no.koredu.android.SmsProcessor;
import no.koredu.android.SmsSender;

import java.util.Map;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class DirectSmsSender implements SmsSender {

  private final String fromPhoneNumber;
  private final ActionTracker tracker;
  private Map<String, SmsProcessor> smsProcessors = Maps.newHashMap();

  public DirectSmsSender(String fromPhoneNumber, ActionTracker tracker) {
    this.fromPhoneNumber = fromPhoneNumber;
    this.tracker = tracker;
  }

  public void addSmsProcessor(String phoneNumber, SmsProcessor smsProcessor) {
    smsProcessors.put(phoneNumber, smsProcessor);
  }

  @Override
  public void send(String phoneNumber, String message) {
    tracker.track(fromPhoneNumber, message, phoneNumber);
    String canonicalPhoneNumber = canonicalize(phoneNumber);
    smsProcessors.get(canonicalPhoneNumber).processMessage(fromPhoneNumber, message);
  }

  private String canonicalize(String phoneNumber) {
    return phoneNumber.startsWith("+") ? phoneNumber : "+47" + phoneNumber.replace(" ", "");
  }
}
