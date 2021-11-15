
/**
 * When you add people
 */

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import java.util.Random;

@WebServlet(name = "Add people", urlPatterns = { "/addPpl" })
public class AddPpl extends HttpServlet {
    static int i = 1; // numéro d’utilisateur
    static Random r = new Random();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        String id = request.getReader().readLine();

        Entity moi = new Entity("User", id);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(moi);

        response.getWriter().print(id);
    }
}