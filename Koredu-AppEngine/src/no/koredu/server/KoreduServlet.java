package no.koredu.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

public abstract class KoreduServlet extends HttpServlet {

  protected final KoreduApi koreduApi = new KoreduApi();
  protected final UserService userService = UserServiceFactory.getUserService();

  private static final ObjectMapper jsonMapper = new ObjectMapper();
  private static final Logger log = Logger.getLogger(KoreduServlet.class.getName());

  protected String getPostData(HttpServletRequest request) throws IOException {
    InputStream in = new BufferedInputStream(request.getInputStream());
    byte[] inputData = new byte[in.available()];
    try {
      ByteStreams.readFully(in, inputData);
    } finally {
      in.close();
    }
    String sessionJson = new String(inputData, Charsets.UTF_8);
    log.info("Received " + sessionJson);
    return sessionJson;
  }

  protected <T> T getPostData(HttpServletRequest request, Class<T> clazz) throws IOException {
    String sessionJson = getPostData(request);
    try {
      return jsonMapper.readValue(sessionJson, clazz);
    } catch (Exception e) {
      throw new RuntimeException("Malformed post data, should be a " + clazz.getName() + " JSON instance, was: " + sessionJson);
    }
  }

  protected Long getPostDataAsLong(HttpServletRequest request) throws IOException {
    String data = getPostData(request);
    return Long.parseLong(unquote(data));
  }

  protected void writeTextResponse(HttpServletResponse response, String data) throws IOException {
    response.setContentType("text/plain");
    PrintWriter writer = response.getWriter();
    writer.print(data);
    writer.close();
  }

  protected String unquote(String s) {
    s = s.trim();
    if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && (s.endsWith("'")))) {
      return s.substring(1, s.length() - 1);
    } else {
      return s;
    }
  }

  protected User getUser() {
    return userService.getCurrentUser();
  }

}
