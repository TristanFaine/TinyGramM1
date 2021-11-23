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

import Profile from "/../views/Profile.js";
import AddUser from "/../views/AddUser.js";
import MockImage from "/../views/MockImage.js";

var app = document.getElementById("app");

m.route(app, "/Splash", {
  "/Splash": Splash,
  "/AddUser": AddUser,
  "/Profile": Profile,
  "/MockImage": MockImage,
  "/MockLoginWall": {
    onmatch: function () {
      //DO NOT DO THAT, LOCALSTORAGE IS INSECURE
      if (!localStorage.getItem("auth-token")) m.route.set("/login");
      else return Splash;
    },
  },
});
