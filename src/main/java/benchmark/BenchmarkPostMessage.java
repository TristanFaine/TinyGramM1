package benchmark;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

@WebServlet(name = "BMPostMessage", urlPatterns = "/bm-postmessage")
public class BenchmarkPostMessage extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter sortie = response.getWriter();

        // How much time does it take to post a message if followed by 10 followers?

        // Initialisation
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity user = new Entity("User", "42");
        HashSet<String> followerList = new HashSet<String>();
        for (int i = 0; i < 10; i++) {
            Entity follower = new Entity("User", "Follower" + i);
            follower.setProperty("name", "Follower " + i);
            datastore.put(follower);
            followerList.add("Follower" + i);
        }
        user.setProperty("followers", followerList);
        datastore.put(user);

        // Test
        long tDébut = System.nanoTime();

        URL url = new URL("http://localhost:8080/_ah/api/api/v1/addPost");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "42");
        con.setRequestProperty(
                "description",
                URLEncoder.encode(
                        "La maison est enfin rénovée... Emménagement la semaine prochaine !",
                        "UTF-8"));
        OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
        writer.write("{\"imageString\": \"wooo\"}");
        writer.close();

        long temps = System.nanoTime() - tDébut;

        response.setCharacterEncoding("Cp1252");
        sortie.println("Temps pour envoyer un message à 10 abonnés : " + (temps / 1e6) + " ms");

        int respCode = con.getResponseCode();
        if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_NOT_FOUND) {
            sortie.println("C’est bon");
        } else {
            sortie.println("C’est pas bon : " + respCode);
        }
    }
}
