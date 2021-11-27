package api;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Api(name = "api", version = "v1", audiences = "852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com", clientIds = "852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com")
public class Endpoint {

    @ApiMethod(name = "salut", httpMethod = HttpMethod.GET)
    public List<String> salut() {
        List<String> l = Arrays.asList("Salut");
        return l;
    }

    @ApiMethod(name = "addUser", httpMethod = HttpMethod.POST, path = "addUser")
    public List<String> addUser(HttpServletRequest req, @Named("id") String id) throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userToken = req.getHeader("Authorization").substring(7);
        Entity user = new Entity("User", id);
        //datastore.put(user);
        
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList("852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com"))
                .build();
                
                GoogleIdToken idToken = verifier.verify(userToken);
                if (idToken != null) {
                    Payload payload = idToken.getPayload();
                    String userId = payload.getSubject();

                    // Do stuff :
                    return Arrays.asList(userId);
                } else {
                    // Cette partie du code est si le token n'est pas reconnu | est expiré
                    return Arrays.asList(userToken);
                }

    }
}