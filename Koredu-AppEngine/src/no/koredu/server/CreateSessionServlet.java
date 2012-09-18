package no.koredu.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.koredu.common.PeeringSession;

public class CreateSessionServlet extends HttpServlet {

  private final KoreduApi koreduApi = new KoreduApi();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PeeringSession session = ServletUtils.getPostData(request, PeeringSession.class);
    koreduApi.createSession(session);
    ServletUtils.writeTextResponse(response, "OK");

  }

}
