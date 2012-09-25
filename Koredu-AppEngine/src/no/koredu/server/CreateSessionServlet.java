package no.koredu.server;

import no.koredu.common.PeeringSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class CreateSessionServlet extends KoreduServlet {

  private static final Logger log = Logger.getLogger(CreateSessionServlet.class.getName());

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    PeeringSession session = getPostData(request, PeeringSession.class);
    koreduApi.createSession(session, getUser());
    writeTextResponse(response, "OK");

  }

}
