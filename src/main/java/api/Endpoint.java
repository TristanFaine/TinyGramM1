package api;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;

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
    //TODO: switch this to a map, so we can expect a "success" OR "error" return
    public List<String> addUser(HttpServletRequest req) throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userToken = req.getHeader("Authorization").substring(7);

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections
                        .singletonList("852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(userToken);
        if (idToken != null) {
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            // Ajout d’un nouvel utilisateur s’il n’existe déjà
            try {
                datastore.get(KeyFactory.createKey("User", userId));
            } catch (EntityNotFoundException e) {
                Entity user = new Entity("User", userId);
                user.setProperty("name", payload.get("given_name") + " " + payload.get("family_name"));
                user.setProperty("following", new HashSet<String>());
                user.setProperty("followers", new HashSet<String>());
                datastore.put(user);
            }
            return Arrays.asList(userId);
        } else {
            return Arrays.asList(
                    "Token invalide :"
                            + userToken);
        }
    }

    @ApiMethod(name = "users", httpMethod = HttpMethod.GET, path = "users")
    public List<Entity> getUsers(HttpServletRequest req) {
        Query q = new Query("User");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
        return result;
    }

    @ApiMethod(name = "follow", httpMethod = HttpMethod.POST, path = "follow")
    public Map<String, Boolean> follow(HttpServletRequest req, @Named("followId") String followId) throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userToken = req.getHeader("Authorization").substring(7);

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections
                        .singletonList("852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(userToken);
        if (idToken != null) {
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();

            if (userId.equals(followId)) {
                return Collections.singletonMap("error(Can'tFollowSelf)", false);
            }
            Entity userFollow = new Entity("User",followId);
            /**
             * Ajoute l'utilisateur ayant fait la requête dans la liste (followers) de l'entité qu'il souhaite follow
             */
            try {
                Entity userFollowChecked = datastore.get(userFollow.getKey()); //Check si l'entité existe
                @SuppressWarnings("unchecked") // Cast ne peut pas vérifier type générique Object
                HashSet<String>followList = (HashSet<String>) userFollowChecked.getProperty("followers");
                if(followList == null){
                    HashSet<String>fallbackList = new HashSet<String>();
                    fallbackList.add(followId);
                    userFollowChecked.setProperty("followers", fallbackList);
                    datastore.put(userFollowChecked);
                } else {
                    followList.add(followId);
                    userFollowChecked.setProperty("followers", followList);
                    datastore.put(userFollowChecked);
                }
            } catch (EntityNotFoundException userFollowChecked) {
                // TODO Auto-generated catch block
                userFollowChecked.printStackTrace();
            }

            /**
             * Ajoute l'entité que souhaite follow l'utilisateur dans son champ (following)
             */
            try {
                Entity userSelf = new Entity("User",userId);
                Entity userSelfChecked=datastore.get(userSelf.getKey());
                @SuppressWarnings("unchecked") // Cast can't verify generic type.
                HashSet<String>followingList = (HashSet<String>) userSelfChecked.getProperty("following");
                if(followingList == null){
                    HashSet<String>fallbackList = new HashSet<String>();
                    fallbackList.add((String)userSelfChecked.getProperty("name"));
                    userSelfChecked.setProperty("following", fallbackList);
                    datastore.put(userSelfChecked);
                } else {
                    followingList.add(followId);
                    userSelfChecked.setProperty("following", followingList);
                    datastore.put(userSelfChecked);
                }
            } catch (EntityNotFoundException userSelfChecked) {
                // TODO Auto-generated catch block
                userSelfChecked.printStackTrace();
            }

            return Collections.singletonMap("result", true);

        }
        return Collections.singletonMap("error(InvalidToken)", false);
    }

    @ApiMethod(name = "addImage", httpMethod = HttpMethod.POST, path = "addImage")
    public Map<String, String> addImage(HttpServletRequest req, @Named("imageString") String imageString,
            @Named("description") String description) throws GeneralSecurityException, IOException, Exception {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userToken = req.getHeader("Authorization").substring(7);
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections
                    .singletonList("852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com"))
            .build();

        GoogleIdToken idToken = verifier.verify(userToken);

        if (idToken != null) {
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();

            String[] parts = imageString.split("[,]");
            imageString = parts[1];
            String fileExtension = parts[0].split("[/]")[1].split("[;]")[0];
            byte[] decode = Base64.getDecoder().decode(imageString);
            //InputStream is = new ByteArrayInputStream(decode);
            /*
             * This sometimes fails for no reason, kept in case we need to re-use
             * try {
             * mimeType = URLConnection.guessContentTypeFromStream(is);
             * String delimiter="[/]";
             * String[] tokens = mimeType.split(delimiter);
             * fileExtension = tokens[1];
             * } catch (IOException ioException){
             * throw new Exception("trucs: " + fileExtension,ioException);
             * }
             */

            String projectId = "projet-tinygram-tf ";
            String bucketName = "projet-tinygram-tf.appspot.com";
            String objectName = userId + description + "." + fileExtension;
            // TODO : do something other than this... but what?

            Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileExtension).build();
            storage.create(blobInfo, decode);

            String imageURL = "http://storage.googleapis.com/" + bucketName + "/" + objectName;
            try {
                datastore.get(KeyFactory.createKey("Post", imageURL));
            } catch (EntityNotFoundException e) {
                Entity post = new Entity("Post", imageURL);
                post.setProperty("userId", userId);
                post.setProperty("creationDate", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                post.setProperty("description", description);
                post.setProperty("likeCounter", 0);
                datastore.put(post);
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put("result", imageURL);
            return map;
        } else {
            // Cette partie du code est si le token n'est pas reconnu | est expiré
            return Collections.singletonMap("error", "Invalid Token");
        }
    }

    @ApiMethod(name = "getPosts", httpMethod = HttpMethod.GET, path = "getPosts")
    public List<Entity> getPosts(HttpServletRequest req, @Named("filter") String filter) throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        if (filter.equals("SubbedOnly")) {
            //TODO: avec userId, recup les trucs où on est inscrit en tant que listener
            String userToken = req.getHeader("Authorization").substring(7);
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections
                        .singletonList("852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com"))
                .build();
            GoogleIdToken idToken = verifier.verify(userToken);

            Query q = new Query("Post").addSort("creationDate", SortDirection.DESCENDING);
            PreparedQuery pq = datastore.prepare(q);
            List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
            return result;
        } else {
            //Pas besoin de s'authentifier pour voir les nouveaux posts, c'est gratis
            //TODO: Changer clé pour avoir ordre par date? Recupérer entité par clé est 2x plus efficace que récuperer par propriété
            Query q = new Query("Post").addSort("creationDate", SortDirection.DESCENDING);
            PreparedQuery pq = datastore.prepare(q);
            List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
            return result;
        }    
    }
        /*
         * Tests :
         * -- 1 : Peut-il obtenir le token via header Authorization? V
         * -- 2 : Peut-il lire correctement le contenu? V
         * -- 3 : Peut-il utiliser les credentials Google correctement? V
         * -- 4 : Peut-il se connecter datastore/cloud storage pour utiliser bucket | V
         * pour l'écriture | V pour la lecture
         */

        /*
         * Trucs à faire :
         * Faire modèle kinds posts (id, imageURL, followedbylist)
         * Peut-être faire un kind followedBy pour pouvoir le mettre à jour?
         * //On suppose qu'un user peut avoir accès aux posts des gens qu'il follow
         * //OU on suppose qu'un post est envoyè aux users inscrits
         * //je sais plus trop c'est quoi la meilleur façon
         * 
         * Produire méthode : getImage | getTimeline
         * 
         * //propriété follow/listener sur post ou user
         * 
         * 
         * 
         */

        //Useful things
        //https://youtu.be/I_E6RIsa2r4 projection queries| getting only keys then querying a subset based on these

        // https://cloud.google.com/appengine/docs/standard/java/using-cloud-storage
        // https://cloud.google.com/storage/docs/reference/libraries#client-libraries-install-java
        // https://cloud.google.com/storage/docs/uploading-objects#storage-upload-object-java
        // https://cloud.google.com/storage/docs/downloading-objects#storage-download-object-java
}