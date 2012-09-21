package no.koredu.server;

import no.koredu.common.InviteReply;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestSessionServlet extends KoreduServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    InviteReply inviteReply = getPostData(request, InviteReply.class);
    koreduApi.requestSession(inviteReply, getUser());
    writeTextResponse(response, "OK");
  }

}
