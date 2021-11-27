
/**
 * Need to check credential works, cloud storage access
 * (Apparently a bucket with 5 GB is available by default)
 */

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;



@WebServlet(name = "Send image", urlPatterns = { "/sendImage" })
@MultipartConfig

public class SendImage extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            //I hope this works with postman or else i might as well just rewrite it completely
            //seems like a lot of spaghetti tbh
            //how would item.isFormField() even work outside a browser?
            String description = "";
            String userName = "";
            String imageString = "";
            String fileName = "";
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();
                    switch(fieldName) {
                        case "description":
                            description = fieldValue;
                            break;
                        case "userName":
                            userName = fieldValue;
                            break;
                        case "userImage":
                            imageString = fieldValue;
                    }
                    // TODO: on peut sûrement faire mieux programmatiquement..
                }
            }

        String userToken = request.getHeader("Authorization").substring(7);
        String test = "what the fuck";
        
        //do stuff with the token here :
        //https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
        //https://developers.google.com/api-client-library/java/google-oauth-java-client/oauth2
        //https://developers.google.com/identity/one-tap/android/idtoken-auth

            
       
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                // Specify the CLIENT_ID of the app that accesses the backend:
                //TODO: put this in secret settings file
                .setAudience(Collections.singletonList("852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com"))
                .build();

                GoogleIdToken idToken = verifier.verify(userToken);
                if (idToken != null) {
                    Payload payload = idToken.getPayload();

                    // Print user identifier
                    String userId = payload.getSubject();

                    // Get profile information from payload
                    String email = payload.getEmail();
                    boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                    String name = (String) payload.get("name");
                    String pictureUrl = (String) payload.get("picture");
                    String locale = (String) payload.get("locale");
                    String familyName = (String) payload.get("family_name");
                    String givenName = (String) payload.get("given_name");

                    // Use or store profile information
                    // ...
                    test = email;

                } else {
                    test = "bruh u ain't logged in";
                }

        test = imageString;
        /*Entity moi = new Entity("User", id);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(moi);
        */

        /* Tests :
            -- 1 : Peut-il obtenir le token via header Authorization? V
            -- 2 : Peut-il lire correctement le body/contenu? V (j'ai pas vérifié InputStream par contre)
            -- 3 : Peut-il utiliser les credentials Google correctement? V
            -- 4 : Peut-il se connecter datastore/cloud storage pour utiliser bucket ?
        */

        //Exporting to a bucket (remember to setup auth first) 
        //https://cloud.google.com/storage/docs/reference/libraries#linux-or-macos
        //https://cloud.google.com/storage/docs/uploading-objects#storage-upload-object-java
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print("\"" + test + "\""); 
        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request.", e);
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
