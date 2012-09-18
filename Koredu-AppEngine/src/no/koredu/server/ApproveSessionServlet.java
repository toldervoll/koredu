package no.koredu.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class ApproveSessionServlet extends HttpServlet {

   private final KoreduApi koreduApi = new KoreduApi();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long sessionId = ServletUtils.getPostDataAsLong(request);
    koreduApi.approveSession(sessionId, true);
    ServletUtils.writeTextResponse(response, "OK");
  }
}
