/**
 * Vue pour les abonnements
 */
var Subscriptions = {
  /**
   * Liste des utilisateurs
   */
  users: m("div", { class: "Subscriptions-List" }, "Chargement en cours..."),
  /**
   * Requête au chargement de la vue
   */
  oncreate: function (vnode) {
    console.log(vnode);
    m.request({
      method: "GET",
      url: "/_ah/api/api/v1/users",
    }).then((res) => {
      Subscriptions.users = m(
        "ul",
        { class: "Subscriptions-List" },
        res.items.map((user) =>
          m("li", [
            user.properties.name,
            m(
              "button",
              {
                class: "Subscriptions-button",
                value: user.key.name,
                onclick: (e) => {
                  m.request({
                    method: "POST",
                    url: "/_ah/api/api/v1/follow",
                    headers: {
                      "Authorization": "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token
                    },
                    params: {
                      followId: e.target.value,
                    },
                  }).then(
                    (res) => {
                      console.log(res);
                      //TODO : if res.truc = false alors ne rien faire
                      //sinon remplacer le texte
                      e.target.innerText ="Se désabonner"; /* Mensonger pour le moment */
                    }
                  );
                },
              },
              "Suivre"
            ),
          ])
        )
      );
    });
  },
  /**
   * Vue des abonnements effectifs et potentiels
   */
  view: function (vnode) {
    if (vnode.attrs.authStatus === undefined || !vnode.attrs.authStatus) {
      return m("div", [
        m(
          "h1",
          {
            id: "userNotConnectedHeader",
          },
          "Veuillez vous connecter pour accéder à ce service."
        ),
      ]);
    } else {
      return [m("h1", "Abonnements"), Subscriptions.users];
    }
  },
};
export default Subscriptions;
