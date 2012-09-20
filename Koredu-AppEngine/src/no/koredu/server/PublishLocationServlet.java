package no.koredu.server;

import no.koredu.common.UserLocation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PublishLocationServlet extends KoreduServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserLocation userLocation = getPostData(request, UserLocation.class);
    koreduApi.publishLocation(userLocation, getUser());
    writeTextResponse(response, "OK");
  }
}
