package no.koredu.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.koredu.common.Verification;

public class ReportVerifiedServlet extends HttpServlet {

  private final KoreduApi koreduApi = new KoreduApi();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Verification verification = ServletUtils.getPostData(request, Verification.class);
    koreduApi.reportVerifiedPhoneNumber(verification);
    ServletUtils.writeTextResponse(response, "OK");
  }


}
