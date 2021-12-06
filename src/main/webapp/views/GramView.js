import Gram from "../models/Gram.js";
var GramView = {
  filter: "All",
  oncreate: () => {
    Gram.loadList(GramView.filter);
  },
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
      return m("div", [
        m(
          "h1",
          {
            id: "postViewHeader",
          },
          "Dernières publications"
        ),
        m(
          "label",
          {
            for: "filter-select",
          },
          "Choisir un filtre :"
        ),
        m(
          "select",
          {
            id: "filter-select",
            onchange: (e) => {
              e.preventDefault();
              e.redraw = false;
              GramView.filter = e.target.value;
              Gram.loadList(GramView.filter);
            },
          },
          [
            m("option", { value: "All" }, "Toutes"),
            m("option", { value: "SubbedOnly" }, "Mes abonnements"),
          ]
        ),
        m("div#PostView", [
          m(
            "div#PostList",
            Gram.loaded
              ? Gram.list.length
                ? Gram.list.map((postData) =>
                    m(
                      "div",
                      {
                        class: "postContainer",
                      },
                      [
                        m(
                          "div",
                          {
                            class: "ownerContainer",
                          },
                          postData.userId
                        ),
                        m("img", {
                          class: "imageContainer",
                          src: postData.imageURL,
                        }),
                        m(
                          "div",
                          {
                            class: "descriptionContainer",
                          },
                          postData.description
                        ),
                        m(
                          "div",
                          {
                            class: "likeContainer",
                          },
                          postData.likeCounter + " likes"
                        ),
                        m(
                          "button",
                          {
                            class: "likeButton",
                            value: postData.key,
                            onclick: (e) => {
                              m.request({
                                method: "POST",
                                url: "/_ah/api/api/v1/likePost",
                                headers: {
                                  "Authorization": "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token
                                },
                                params: {
                                  postId: e.target.value,
                                },
                              }).then(
                                (res) => {
                                  console.log(res);
                                  if (res.error === undefined) {
                                    postData.likeCounter +=1;
                                    e.target.innerText ="Unlike"; /* Mensonger pour le moment */
                                    
                                  }
                                  
                                }
                              );
                            },
                          },
                          "Like"
                        ),

                      ]
                    )
                  )
                : "🗋 Rien à afficher pour le moment..."
              : "Chargement..."
          ),
        ]),
      ]);
    }
  },
};

export default GramView;
