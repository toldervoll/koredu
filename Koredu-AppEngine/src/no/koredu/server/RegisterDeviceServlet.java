package no.koredu.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegisterDeviceServlet extends KoreduServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String deviceId = getPostData(request);
    koreduApi.registerDevice(deviceId, getUser());
    writeTextResponse(response, "OK");
  }

}
