package no.koredu.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterDeviceServlet extends HttpServlet {

  private final KoreduApi koreduApi = new KoreduApi();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String deviceId = ServletUtils.getPostData(request);
    koreduApi.registerDevice(deviceId);
    ServletUtils.writeTextResponse(response, "OK");

  }

}
