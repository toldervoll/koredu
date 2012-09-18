package no.koredu.server;

import no.koredu.common.UserLocation;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PublishLocationServlet extends HttpServlet {

   private final KoreduApi koreduApi = new KoreduApi();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserLocation userLocation = ServletUtils.getPostData(request, UserLocation.class);
    koreduApi.publishLocation(userLocation);
    ServletUtils.writeTextResponse(response, "OK");
  }
}
