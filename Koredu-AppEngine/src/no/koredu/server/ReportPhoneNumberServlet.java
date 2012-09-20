package no.koredu.server;

import no.koredu.common.Verification;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ReportPhoneNumberServlet extends KoreduServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Verification verification = getPostData(request, Verification.class);
    koreduApi.reportPhoneNumber(verification, getUser());
    writeTextResponse(response, "OK");

  }

}
