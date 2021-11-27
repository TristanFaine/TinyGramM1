//NOTE: techniquement, ce fichier ne sert pas vraiment au routage
//mais il liste les types de chemins possibles, ainsi que la vue de la page par defaut.

var Splash = {
  view: function () {
    return m("div", [
      m(
        "h1",
        {
          class: "title",
        },
        "Tinygram"
      ),
      m("div", [
        m(
          "a",
          {
            href: "#!/AddUser",
          },
          "Ajouter un utilisateur"
        ),
      ]),
    ]);
  },
};
//TODO: ^ manually adding an a href for every new page is a pain. maybe figure something
//to do it automatically.

import UnknownProfile from "/../views/UnknownProfile.js";
import Profile from "/../views/Profile.js";
import AddUser from "/../views/AddUser.js";
import SendImage from "/../views/SendImage.js";


var app = document.getElementById("app");

//Note: Si on refresh la page, les conditions ne sont pas respectées donc on se tape .. hmm.



//funnily enough, this should break on load since googleauth is undefined, but i suppose mithril is smart
m.route(app, "/Splash", {
  "/Splash": Splash,
  "/AddUser": AddUser,
  "/UnknownProfile": UnknownProfile,
  "/Profile": {
    onmatch: function () {
      if (GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE) === undefined || !GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE)) m.route.set("/UnknownProfile");
      else return Profile;
    }
  },
  "/SendImage": {
    onmatch: function () {
      if (GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE) === undefined || !GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE)) m.route.set("/Splash");
      else return SendImage;
    },
  },
});
