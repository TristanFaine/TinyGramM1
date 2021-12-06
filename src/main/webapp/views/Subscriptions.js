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
    m.request({
      method: "GET",
      headers: {
        Authorization:
          "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token,
      },
      url: "/_ah/api/api/v1/users",
    }).then((res) => {
      console.log(res);
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
                style: "margin-left: 1em",
                value: user.key.name,
                disabled: user.properties.hasFollowed,
                onclick: (e) => {
                  m.request({
                    method: "POST",
                    url: "/_ah/api/api/v1/follow",
                    headers: {
                      Authorization:
                        "Bearer " +
                        GoogleAuth.currentUser.get().getAuthResponse().id_token,
                    },
                    params: {
                      followId: e.target.value,
                    },
                  }).then((res) => {
                    console.log(res);
                    if (res.error === undefined) {
                      e.target.innerHTML = "✔️ Suivi";
                      e.target.disabled = true;
                    }
                  });
                },
              },
              user.properties.hasFollowed ? "✔️ Suivi" : "Suivre"
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
