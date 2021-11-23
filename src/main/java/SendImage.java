
/**
 * Need to check credential works, cloud storage access
 * (Apparently a bucket with 5 GB is available by default)
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

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
            String description = "";
            String userName = "";
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
                    }
                    // TODO: on peut sûrement faire mieux programmatiquement..


                } else {
                    // Process form file field (input type="file").
                    String fieldName = item.getFieldName();
                    //String fileName = FilenameUtils.getName(item.getName());
                    InputStream fileContent = item.getInputStream();
                    // Voir comment s'utiliser ceci ^
                    //Notre fieldName correspond bien au base64 de l'image.. mais est-ce une bonne idée
                    //de le récupérer ainsi..?
                }
            }
        String userToken = request.getHeader("Authorization");
        
        // we're receiving image as Base64 string, might switch to strictly binary
        //String userToken = request.getHeader("Authorization").substring(7);
        //String description = System.getProperty("com.google.appengine.runtime.version");
        //https://stackoverflow.com/questions/2422468/how-can-i-upload-files-to-a-server-using-jsp-servlet/2424824#2424824
        



        //do stuff with the token here :
        //https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
        //https://developers.google.com/api-client-library/java/google-oauth-java-client/oauth2

        /*Entity moi = new Entity("User", id);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(moi);
        */

        /* Tests :
            -- 1 : Peut-il obtenir le token via header Authorization? V
            -- 2 : Peut-il lire correctement le body/contenu? V (j'ai pas vérifié InputStream par contre)
            -- 3 : Peut-il utiliser les credentials Google correctement? ?
            -- 4 : Peut-il se connecter datastore/cloud storage pour utiliser bucket? ?
        */

        //Exporting to a bucket (remember to setup auth first) 
        //https://cloud.google.com/storage/docs/reference/libraries#linux-or-macos
        //https://cloud.google.com/storage/docs/uploading-objects#storage-upload-object-java
        //

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print("\"" + description + "\""); 
        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request.", e);
        }
    }
}