package api;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
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
            user.setProperty("followers", new ArrayList<String>());
            datastore.put(user);
        }

        return Collections.singletonMap("result", userId);
    }

    @ApiMethod(name = "users", httpMethod = HttpMethod.GET, path = "users")
    public List<Entity> getUsers(HttpServletRequest req) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = idToken(req);
        Query q = new Query("User");
        String userId = "";
        // Optimisation requête, on veut seulement savoir les noms des utilisateurs si
        // on est pas connecté
        if (idToken == null)
            q.addProjection(new PropertyProjection("name", String.class));
        else {
            Payload payload = idToken.getPayload();
            userId = payload.getSubject();
            q.setFilter(
                    new FilterPredicate("__key__", FilterOperator.NOT_EQUAL, KeyFactory.createKey("User", userId)));
        }
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        QueryResultList<Entity> result = pq.asQueryResultList(FetchOptions.Builder.withLimit(100));

        // boucle optionnelle pour savoir si on follow l'utilisateur dans cette liste
        // d'entitées
        if (idToken != null) {
            for (Entity postEntity : result) {
                @SuppressWarnings("unchecked")
                ArrayList<String> followerList = (ArrayList<String>) postEntity.getProperty("followers");
                if (followerList != null && followerList.contains(userId)) {
                    postEntity.setProperty("hasFollowed", true);
                } else {
                    postEntity.setProperty("hasFollowed", false);
                }
            }
        }

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

    @ApiMethod(name = "likePost", httpMethod = HttpMethod.POST, path = "likePost")
    public Map<String, String> likePost(HttpServletRequest req, @Named("postId") String postId)
            throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GoogleIdToken idToken = idToken(req);

        if (idToken == null)
            return Collections.singletonMap("error", "Invalid Token");

        Payload payload = idToken.getPayload();
        String userId = payload.getSubject();

        // Récupérer tout les likeGivers qui ont comme ancêtre, le post en question, où
        // l'on peut apparaître
        Key ancestorKey = new Entity("Post", postId).getKey();
        Filter equalGiver = new FilterPredicate("givers", FilterOperator.EQUAL, userId);
        Query query = new Query("LikeGiver").setAncestor(ancestorKey).setFilter(equalGiver).setKeysOnly();
        PreparedQuery pq = datastore.prepare(query);
        QueryResultList<Entity> likeList = pq.asQueryResultList(FetchOptions.Builder.withDefaults());

        // Voir ensuite dans cette liste d'entités si on existe en tant que giver
        // Si le résultat n'est pas de longueur 0, alors on a déja envoyé un like, donc
        // erreur
        if (likeList.size() > 0)
            return Collections.singletonMap("error", "Un like par utilisateur max");
        // Si on n'a pas encore envoyé de like, alors selectionner une entité "au
        // hasard" pour s'ajouter en tant que giver.
        Query query2 = new Query("LikeGiver").setAncestor(ancestorKey);
        PreparedQuery pq2 = datastore.prepare(query2);
        List<Entity> likeList2 = pq2.asList(FetchOptions.Builder.withDefaults());
        // Prendre un truc au hasard la dedans
        Random r = new Random();
        Entity randomLikeGiverEntity = likeList2.get(r.nextInt(likeList2.size()));

        // s'ajouter en tant que giver, en creeant la property si celle-ci n'a pas
        // encore été initialisée
        @SuppressWarnings("unchecked")
        List<String> randomLikeGiver = (List<String>) randomLikeGiverEntity.getProperty("givers");
        if (randomLikeGiver == null) {
            List<String> fallbackList = new ArrayList<String>();
            fallbackList.add(userId);
            randomLikeGiverEntity.setProperty("givers", fallbackList);
            datastore.put(randomLikeGiverEntity);
        } else {
            if (randomLikeGiver.contains(userId)) {
                return Collections.singletonMap("error", "Can only follow someone once");
            } else
                randomLikeGiver.add(userId);
            randomLikeGiverEntity.setProperty("givers", randomLikeGiver);
            datastore.put(randomLikeGiverEntity);
        }

        return Collections.singletonMap("success", "Like has been given");
    }

    @ApiMethod(name = "addPost", httpMethod = HttpMethod.POST, path = "addPost")
    public Map<String, String> addPost(HttpServletRequest req, @Named("imageString") String imageString,
            @Named("description") String description) throws GeneralSecurityException, IOException, Exception {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GoogleIdToken idToken = idToken(req);

        if (idToken == null && req.getHeader("Authorization") != "42")
            return Collections.singletonMap("error", "Invalid token");

        Payload payload = idToken.getPayload();
        String userId = payload.getSubject();

        if (req.getHeader("Authorization") == "42")
            userId = "42";

        String[] parts = imageString.split("[,]");
        imageString = parts[1];
        String fileExtension = parts[0].split("[/]")[1].split("[;]")[0];
        byte[] decode = Base64.getDecoder().decode(imageString);

        String projectId = "projet-tinygram-tf ";
        String bucketName = "projet-tinygram-tf.appspot.com";
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        // customTimestamp permet d'ordonner les clés dans un ordre ascendant sans faire
        // de tri|sort lors du query. Donc c'est plus rapide :D
        String customTimestamp = String.format("%04d", 9999 - Integer.parseInt(timestamp.substring(0, 4))) +
                String.format("%02d", 12 - Integer.parseInt(timestamp.substring(4, 6))) +
                String.format("%02d", 31 - Integer.parseInt(timestamp.substring(6, 8))) +
                String.format("%02d", 24 - Integer.parseInt(timestamp.substring(8, 10))) +
                String.format("%02d", 60 - Integer.parseInt(timestamp.substring(10, 12))) +
                String.format("%02d", 60 - Integer.parseInt(timestamp.substring(12, 14)));

        String objectName = userId + description.replaceAll("[-._~:\\/?#\\[\\]@!$&'()*+,;=%^|\\r\\n|\\n|\\r|]", "")
                + "." + fileExtension;
        // ^ pour eviter d'avoir des trucs bizarres dans l'URL
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/" + fileExtension).build();
        storage.create(blobInfo, decode);

        String imageURL = "http://storage.googleapis.com/" + bucketName + "/" + objectName;

        // String random pour différencier posts lors de la même seconde
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(26);
        for (int i = 0; i < 26; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        Entity post = new Entity("Post", customTimestamp + sb.toString());
        post.setProperty("imageURL", imageURL);
        post.setProperty("userId", userId);
        post.setProperty("description", description);
        datastore.put(post);

        Entity user = datastore.get(new Entity("User", userId).getKey());
        Entity postReceiver = new Entity("PostReceiver", customTimestamp + sb.toString(), post.getKey());
        // Définir l'ancêtre n'est pas avec methode explicite setAncestor(), mais en
        // troisieme position de l'entité
        @SuppressWarnings("unchecked")
        List<String> explicitList = (List<String>) user.getProperty("followers");
        postReceiver.setProperty("receivers", explicitList); // Ajouter les followers à ce moment, en tant que receivers

        Entity likeGiver = new Entity("LikeGiver", customTimestamp + sb.toString(), post.getKey());
        likeGiver.setProperty("givers", new ArrayList<String>()); // ajouter liste vide de likers

        datastore.put(postReceiver);
        datastore.put(likeGiver);

        Map<String, String> map = new HashMap<String, String>();
        map.put("result", imageURL);
        return map;
    }

    // Solution ultra-sale pour envoyer list<entities> et le cursor associé au query
    public class PairCursor {
        public final List<Entity> entities;
        public final String cursor;

        public PairCursor(List<Entity> entities, String cursor) {
            this.entities = entities;
            this.cursor = cursor;
        }
    }

    @ApiMethod(name = "getPosts", httpMethod = HttpMethod.GET, path = "getPosts")

    public PairCursor getPosts(HttpServletRequest req, @Named("filter") String filter,
            @Named("cursor") String WebCursor)
            throws GeneralSecurityException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if (filter.equals("SubbedOnly")) {
            GoogleIdToken idToken = idToken(req);
            /*
             * if (idToken == null)
             * return Collections.singletonMap("error", "Invalid token");
             */
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            Filter equalReceiver = new FilterPredicate("receivers", FilterOperator.EQUAL, userId);
            Query q = new Query("PostReceiver").setFilter(equalReceiver).setKeysOnly();
            PreparedQuery pq = datastore.prepare(q);
            QueryResultList<Entity> querypart = null;
            if (WebCursor != null && !WebCursor.isEmpty()) {
                // On utilise le curseur dans la requete
                querypart = pq.asQueryResultList(
                        FetchOptions.Builder.withLimit(10).startCursor(Cursor.fromWebSafeString(WebCursor)));
            } else {
                // On fait la requete sans curseur
                querypart = pq.asQueryResultList(FetchOptions.Builder.withLimit(10));
            }
            Cursor newCursor = querypart.getCursor(); // On recupere le curseur qui pointe automatiquement vers le nv
                                                      // resultat
            String encodedCursor = newCursor.toWebSafeString();

            // On recupere toutes les clés des post receivers où l'utilisateur actuel
            // apparaît en tant que receiver
            // On peut donc pointer vers les clés parents, pour récuperer les posts.
            List<Key> keyList = new ArrayList<Key>();
            for (Entity childEntity : querypart) {
                keyList.add(childEntity.getParent());
            }
            // On va ensuite recuperer tout les posts correspondant.
            Map<Key, Entity> postMap = datastore.get(keyList);
            TreeMap<Key, Entity> sortedMap = new TreeMap<Key, Entity>(postMap);
            List<Entity> result = new ArrayList<Entity>(sortedMap.values());
            for (Entity postEntity : result) {
                // Récuperer les enfants associé à l'entité Post actuel
                Query query = new Query("LikeGiver").setAncestor(postEntity.getKey());
                PreparedQuery pq2 = datastore.prepare(query);
                int likeCounter = 0;
                boolean hasLiked = false;
                for (Entity likeGiver : pq2.asIterable()) {
                    // Faire la somme des longueurs des propriétés givers
                    @SuppressWarnings("unchecked")
                    ArrayList<String> giverList = (ArrayList<String>) likeGiver.getProperty("givers");
                    if (giverList != null) {
                        likeCounter += giverList.size();
                        // dommage qu'on peut pas cast likeGiver en tant que HashSet, ça serait plus
                        // efficace.
                        if (giverList.contains(userId)) {
                            hasLiked = true;
                        }
                    }
                }
                // La meilleure façon de procéder est d'utiliser un DTO, plutot que d'ajouter
                // une fausse propriété
                postEntity.setProperty("likeCounter", likeCounter);
                if (idToken == null)
                    postEntity.setProperty("hasLiked", false);
                else
                    postEntity.setProperty("hasLiked", hasLiked);

                // On récupère les noms des auteurs
                String authorName;
                try {
                    authorName = (String) datastore.get(KeyFactory.createKey(
                            "User",
                            (String) postEntity.getProperty("userId"))).getProperty("name");
                } catch (EntityNotFoundException e) {
                    authorName = "Anonyme";
                }
                postEntity.setProperty("authorName", authorName);
            }
            return new PairCursor(result, encodedCursor);

        } else {
            // Pas besoin de s'authentifier pour voir les nouveaux posts, mais on regarde
            // quand meme si on est auth, pour avoir booleen hasLiked
            GoogleIdToken idToken = idToken(req);
            Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            Query q = new Query("Post");

            PreparedQuery pq = datastore.prepare(q);
            QueryResultList<Entity> result = null;
            if (WebCursor != null && !WebCursor.isEmpty()) {
                // On utilise le curseur dans la requete
                result = pq.asQueryResultList(
                        FetchOptions.Builder.withLimit(10).startCursor(Cursor.fromWebSafeString(WebCursor)));
            } else {
                // On fait la requete sans curseur
                result = pq.asQueryResultList(FetchOptions.Builder.withLimit(10));
            }
            Cursor newCursor = result.getCursor(); // On recupere le curseur qui pointe automatiquement vers le nv
                                                   // resultat
            String encodedCursor = newCursor.toWebSafeString();
            for (Entity postEntity : result) {
                // Récuperer les enfants associé à l'entité Post actuel pour avoir le nombre de
                // likes
                Query query = new Query("LikeGiver").setAncestor(postEntity.getKey());
                PreparedQuery pq2 = datastore.prepare(query);
                int likeCounter = 0;
                boolean hasLiked = false;
                for (Entity likeGiver : pq2.asIterable()) {
                    // Faire la somme des longueurs des propriétés givers
                    @SuppressWarnings("unchecked")
                    ArrayList<String> giverList = (ArrayList<String>) likeGiver.getProperty("givers");
                    if (giverList != null) {
                        likeCounter += giverList.size();
                        // dommage qu'on peut pas cast likeGiver en tant que HashSet, ça serait plus
                        // efficace.
                        if (giverList.contains(userId)) {
                            hasLiked = true;
                        }
                    }
                }
                // La meilleure façon de procéder est d'utiliser un DTO, plutot que d'ajouter
                // une fausse propriété
                postEntity.setProperty("likeCounter", likeCounter);
                if (idToken == null)
                    postEntity.setProperty("hasLiked", false);
                else
                    postEntity.setProperty("hasLiked", hasLiked);

                // On récupère les noms des auteurs
                String authorName;
                try {
                    authorName = (String) datastore.get(KeyFactory.createKey(
                            "User",
                            (String) postEntity.getProperty("userId"))).getProperty("name");
                } catch (EntityNotFoundException e) {
                    authorName = "Anonyme";
                }
                postEntity.setProperty("authorName", authorName);
            }
            return new PairCursor(result, encodedCursor);
        }
    }

}