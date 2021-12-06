package benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@WebServlet(name = "BMPostMessage", urlPatterns = "/bm-postmessage")
public class BenchmarkPostMessage extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // How much time does it take to post a message if followed by 10 followers?
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity mlle10 = new Entity("User", "Mlle10");
        mlle10.setProperty("name", "Mlle X");
        HashSet<String> followerList = new HashSet<String>();
        for (int i = 0; i < 10; i++) {
            Entity follower = new Entity("User", "Follower" + i);
            follower.setProperty("name", "Follower " + i);
            datastore.put(follower);
            followerList.add("Follower" + i);
        }
        mlle10.setProperty("followers", followerList);
        datastore.put(mlle10);
        response.getWriter().print("OK");
    }
}
