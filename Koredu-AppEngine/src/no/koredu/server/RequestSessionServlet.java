package no.koredu.server;

import no.koredu.common.PeeringSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestSessionServlet extends HttpServlet {

  private final KoreduApi koreduApi = new KoreduApi();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PeeringSession session = ServletUtils.getPostData(request, PeeringSession.class);
    koreduApi.requestSession(session);
    ServletUtils.writeTextResponse(response, "OK");
  }

}
