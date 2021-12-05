package api;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.Filter;
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

    /**
     * Jeton de sécurité garantissant l’authentification Google
     * 
     * @param req requête H.T.T.P. associée
     * @return idToken
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private GoogleIdToken idToken(HttpServletRequest req) throws GeneralSecurityException, IOException {
        String userToken = req.getHeader("Authorization").substring(7);

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections
                        .singletonList("852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com"))
                .build();
        GoogleIdToken idToken = verifier.verify(userToken);
        return idToken;
    }

    @ApiMethod(name = "addUser", httpMethod = HttpMethod.POST, path = "addUser")
    public Map<String, String> addUser(HttpServletRequest req) throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GoogleIdToken idToken = idToken(req);
        if (idToken == null)
            return Collections.singletonMap(
                    "error",
                    "Token invalide :" + req.getHeader("Authorization"));

        Payload payload = idToken.getPayload();
        String userId = payload.getSubject();

        // Ajout d’un nouvel utilisateur s’il n’existe déjà
        try {
            datastore.get(KeyFactory.createKey("User", userId));
        } catch (EntityNotFoundException e) {
            Entity user = new Entity("User", userId);
            user.setProperty("name", payload.get("given_name") + " " + payload.get("family_name"));
            user.setProperty("followers", new HashSet<String>());
            datastore.put(user);
        }

        return Collections.singletonMap("result", userId);
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
    public Map<String, String> follow(HttpServletRequest req, @Named("followId") String followId)
            throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GoogleIdToken idToken = idToken(req);

        if (idToken == null)
            return Collections.singletonMap("error", "Invalid Token");

        Payload payload = idToken.getPayload();
        String userId = payload.getSubject();

        if (userId.equals(followId)) {
            return Collections.singletonMap("error", "Cannot follow oneself");
        }
        Entity userFollow = new Entity("User", followId);
        /**
         * Ajoute l'utilisateur ayant fait la requête dans la liste (followers) de
         * l'entité qu'il souhaite follow
         */
        try {
            Entity userFollowChecked = datastore.get(userFollow.getKey()); // Check si l'entité existe
            @SuppressWarnings("unchecked") // Cast ne peut pas vérifier type générique Object

            List<String> followList = (List<String>) userFollowChecked.getProperty("followers");

            if (followList == null) {
                List<String> fallbackList = new ArrayList<String>();
                fallbackList.add(userId);
                userFollowChecked.setProperty("followers", fallbackList);
                datastore.put(userFollowChecked);
            } else {
                if (followList.contains(userId)) {
                    return Collections.singletonMap("error", "Can only follow someone once");
                } else
                    followList.add(userId);
                userFollowChecked.setProperty("followers", followList);
                datastore.put(userFollowChecked);
            }
        } catch (EntityNotFoundException userFollowChecked) {
            userFollowChecked.printStackTrace();
        }
        return Collections.singletonMap("result", "No problem");
    }

    @ApiMethod(name = "addPost", httpMethod = HttpMethod.POST, path = "addPost")
    public Map<String, String> addPost(HttpServletRequest req, @Named("imageString") String imageString,
            @Named("description") String description) throws GeneralSecurityException, IOException, Exception {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GoogleIdToken idToken = idToken(req);

        if (idToken == null)
            return Collections.singletonMap("error", "Invalid token");

        Payload payload = idToken.getPayload();
        String userId = payload.getSubject();

        String[] parts = imageString.split("[,]");
        imageString = parts[1];
        String fileExtension = parts[0].split("[/]")[1].split("[;]")[0];
        byte[] decode = Base64.getDecoder().decode(imageString);

        String projectId = "projet-tinygram-tf ";
        String bucketName = "projet-tinygram-tf.appspot.com";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        String objectName = userId + description + "." + fileExtension;

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileExtension).build();
        storage.create(blobInfo, decode);

        String imageURL = "http://storage.googleapis.com/" + bucketName + "/" + objectName;

        // TODO: define likecounter as a children thing
        // https://docs.google.com/presentation/d/1rjrl-mPkG6zUukZeE_bbA8QU7PqNWu6HHHoBF1H17dQ/edit#slide=id.p29
        // voir aux alentours de la slide 24
        // String random pour différencier posts lors de la même seconde
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(26);
        for (int i = 0; i < 26; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        Entity post = new Entity("Post", timestamp + sb.toString());
        post.setProperty("imageURL", imageURL);
        post.setProperty("userId", userId);
        post.setProperty("description", description);
        post.setProperty("likeCounter", 0);
        datastore.put(post);


        //NOTE: let us pray this scales..
        Entity user = datastore.get(new Entity("User", userId).getKey());
        Entity postReceiver = new Entity("PostReceiver", post.getKey());
        //Définir l'ancêtre n'est pas avec methode explicite setAncestor(), mais ainsi ^
        //Ajouter les followers à ce moment, en tant que receivers

        @SuppressWarnings("unchecked")
        List<String> explicitList = (List<String>) user.getProperty("followers");
        postReceiver.setProperty("receivers", explicitList);
        datastore.put(postReceiver);

        Map<String, String> map = new HashMap<String, String>();
        map.put("result", imageURL);
        return map;
    }

    @ApiMethod(name = "getPosts", httpMethod = HttpMethod.GET, path = "getPosts")
    public List<Entity> getPosts(HttpServletRequest req, @Named("filter") String filter)
            throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if (filter.equals("SubbedOnly")) {
            GoogleIdToken idToken = idToken(req);
            /*
            if (idToken == null)
                return Collections.singletonMap("error", "Invalid token");
            */
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            //array is empty, try to figure out why i guess
            Filter equalReceiver = new FilterPredicate("receivers", FilterOperator.EQUAL, userId);
            Query q = new Query("PostReceiver").setFilter(equalReceiver).setKeysOnly();
            //On recupere toutes les clés des post receivers où l'utilisateur actuel apparaît en tant que receiver
            //On peut donc pointer vers les clés parents, pour récuperer les posts.
            List<Key> keyList = new ArrayList<Key>();
            for (Entity childEntity : datastore.prepare(q).asIterable()) {
                keyList.add(childEntity.getParent());
              }
            //On va ensuite recuperer tout les posts correspondant.
            
            Map<Key, Entity> postMap = datastore.get(keyList);
            List<Entity> result = new ArrayList<Entity>(postMap.values());
            return result;

        } else {
            // Pas besoin de s'authentifier pour voir les nouveaux posts, la maison offre
            Query q = new Query("Post").addSort("__key__", SortDirection.DESCENDING);
            PreparedQuery pq = datastore.prepare(q);
            List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
            return result;
        }
    }

    /*
     * Trucs à faire :
     * Implémenter une méthode pour incrémenter le compteurLike d'un post
     * 
     * 
     * 
     */

}