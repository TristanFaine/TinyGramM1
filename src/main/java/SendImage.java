
/**
 * This is rubbish for now, just checking the API works back and forth
 * Also never use cloud for java dev holy shit
 */

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

//pour utiliser getpart????? servlet version 3.1 > 3.0 pourtant.??
//il reconnait pas class Part


@WebServlet(name = "Send image", urlPatterns = { "/sendImage" })
@MultipartConfig
//? il ne reconnait pas multipart config

public class SendImage extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // we're receiving image as Base64 string, might switch to strictly binary
        response.setContentType("multipart/mixed");
        String id = request.getParameter("id");
        String description = request.getParameter("description");
        //https://stackoverflow.com/questions/2422468/how-can-i-upload-files-to-a-server-using-jsp-servlet/2424824#2424824
        //???? on est bien en servlet 3.1 pourtant depuis le pom.xml, alors pourquoi il pige pas?
        /*
        Part filePart = request.getPart("userImage"); // Retrieves <input type="file" name="file">
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
        InputStream fileContent = filePart.getInputStream();
        */

        //do stuff with the token here :
        //https://developers.google.com/api-client-library/java/google-api-java-client/oauth2

        /*Entity moi = new Entity("User", id);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(moi);
        */

        /* Tests :
            -- 1 : Can it read the token in bearer header ? 
            -- 2 : Can it read the parameter "description" ?
            -- 3 : Can it read the file content, and send it again I guess
            -- 4 : Can it connect properly to the datastore to do stuff


        */

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print("\"" + description + "\""); 
    }
}