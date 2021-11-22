
/**
 * This is rubbish for now, just checking the API works back and forth
 */

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;


@WebServlet(name = "Send image", urlPatterns = { "/sendImage" })
public class SendImage extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // we're receiving a blob.. because just recieving the image value wouldn't allow us to
        // set datastore values, aka Owner, or stuff like date, height, size, etc..
        
        response.setContentType("image/gif");
        response.setCharacterEncoding("UTF-8");
        String id = request.getParameter("id");

        Entity moi = new Entity("User", id);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(moi);

        response.getWriter().print("\"" + id + "\""); 
    }
}