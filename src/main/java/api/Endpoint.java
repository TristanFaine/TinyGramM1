package api;

import java.util.Arrays;
import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@Api(name = "api", version = "v1", audiences = "852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com", clientIds = "852760108989-gqn73cl4kuk3nb5a8mgf38rgace4u3lk.apps.googleusercontent.com")
public class Endpoint {

    @ApiMethod(name = "salut", httpMethod = HttpMethod.GET)
    public List<String> salut() {
        List<String> l = Arrays.asList("Salut");
        return l;
    }

    @ApiMethod(name = "addUser", httpMethod = HttpMethod.POST, path = "addUser")
    public List<String> addUser(@Named("id") String id) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity user = new Entity("User", id);
        datastore.put(user);
        return Arrays.asList(id);
    }
}