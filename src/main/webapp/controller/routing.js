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
    ]);
  },
};

import UnknownProfile from "/../views/UnknownProfile.js";
import Profile from "/../views/Profile.js";
import SendImage from "/../views/SendImage.js";
import Subscriptions from "/../views/Subscriptions.js";
import GramView from "/../views/GramView.js";

var app = document.getElementById("app");

//funnily enough, this should break on load since googleauth is undefined, but i suppose mithril is smart
m.route(app, "/Splash", {
  "/Splash": Splash,
  "/UnknownProfile": UnknownProfile,
  "/Profile": {
    onmatch: function () {
      if (
        GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE) === undefined ||
        !GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE)
      )
        m.route.set("/UnknownProfile");
      else return Profile;
    },
  },
  "/SendImage": {
    onmatch: function () {
      if (
        GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE) === undefined ||
        !GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE)
      )
        m.route.set("/Splash");
      else return SendImage;
    },
  },
  "/Subscriptions": {
    onmatch: function () {
      if (
        GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE) === undefined ||
        !GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE)
      )
        m.route.set("/Splash");
      else return Subscriptions;
    },
  },
  "/GramView": {
    onmatch: function () {
      if (
        GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE) === undefined ||
        !GoogleAuth.currentUser.get().hasGrantedScopes(SCOPE)
      )
        m.route.set("/Splash");
      else return GramView;
    },
  },
});
