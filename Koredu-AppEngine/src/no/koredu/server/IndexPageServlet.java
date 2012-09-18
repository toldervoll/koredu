package no.koredu.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class IndexPageServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println("<html>");
    out.println("<head><title>Koredu.no?</title>");
    out.println("<script type=\"text/javascript\">\n" +
        "\n" +
        "  var _gaq = _gaq || [];\n" +
        "  _gaq.push(['_setAccount', 'UA-34818746-1']);\n" +
        "  _gaq.push(['_trackPageview']);\n" +
        "\n" +
        "  (function() {\n" +
        "    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n" +
        "    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n" +
        "    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n" +
        "  })();\n" +
        "\n" +
        "</script>");
    out.println("</head");
    out.println("<body>Coming soon...</body>");
    out.println("</html>");
  }
}
