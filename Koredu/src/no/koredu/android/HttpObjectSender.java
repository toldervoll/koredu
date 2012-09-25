package no.koredu.android;

import android.content.Context;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class HttpObjectSender implements ObjectSender {

  private final Context context;
  private final JsonMapper jsonMapper;

  public HttpObjectSender(Context context, JsonMapper jsonMapper) {
    this.context = context;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void send(String path, Object object) {
    String json = jsonMapper.toJson(object);
    HttpService.post(context, path, json);
  }

  @Override
  public String syncSend(String path, Object object) {
    String json = jsonMapper.toJson(object);
    return HttpService.sendToServer(path, json);
  }
}
