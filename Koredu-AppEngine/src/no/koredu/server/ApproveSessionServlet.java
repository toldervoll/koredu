package no.koredu.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class ApproveSessionServlet extends KoreduServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long sessionId = getPostDataAsLong(request);
    koreduApi.approveSession(sessionId, true, getUser());
    writeTextResponse(response, "OK");
  }
}
