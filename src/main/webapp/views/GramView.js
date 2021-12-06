import Gram from "../models/Gram.js";
var GramView = {
  filter: "All",
  //TODO : en vrai on a pas besoin des propriétés current... faut juste faire une condition if else à chaque utilisation de loadList, et pour l'affichage de pageNumber
  currentCursorList: ["",""],
  cursorListAll: ["",""], //TODO :simuler "page précédente" via une liste d'anciens curseurs
  cursorListFollow: ["",""],
  currentPageNumber: 1,
  pageNumberAll: 1,
  pageNumberFollow: 1,
  //TODO:bouton "page suivante" et bouton "reset pagination"
  //Logique de traitement : utiliser la numerotation de page pour selectionner le cursor à utiliser.
  oncreate: () => {
    //load special où on change pas le curseur, mais il faut quand meme recup la valeur du curseur lors de la premiere init
    Gram.loadList(GramView.filter, GramView.currentCursorList[GramView.currentPageNumber-1]).then(function() {
      if (GramView.filter == "All") {
        GramView.currentCursorList[GramView.currentPageNumber] = Gram.cursor;
        GramView.cursorListAll[GramView.currentPageNumber] = Gram.cursor;
      } else {
        GramView.currentCursorList[GramView.currentPageNumber] = Gram.cursor;
        GramView.cursorListFollow[GramView.currentPageNumber] = Gram.cursor;
      }
      document.getElementById("nextPageButton").value = Gram.cursor;
    })
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
          "button",
          {
            id: "nextPageButton",
            value: GramView.currentCursorList[GramView.currentPageNumber],
            onclick: (e) => {
              e.preventDefault();
              e.redraw = false;
              //Donner page suivante, donc incrémenter le compteur, et insérer un nouveau curseur
              Gram.loadList(GramView.filter, e.target.value).then(function() {
                if (GramView.filter == "All") {
                  GramView.currentCursorList[GramView.currentPageNumber + 1] = Gram.cursor;
                  GramView.cursorListAll[GramView.currentPageNumber + 1] = Gram.cursor;
                  GramView.pageNumberAll +=1;
                } else {
                  GramView.currentCursorList[GramView.currentPageNumber + 1] = Gram.cursor;
                  GramView.cursorListFollow[GramView.currentPageNumber + 1] = Gram.cursor;
                  GramView.pageNumberFollow +=1;
                }
                document.getElementById("nextPageButton").value = Gram.cursor;
                GramView.currentPageNumber +=1;
              })
            }
          },
          "Page Suivante"
        ),
        m(
          "button",
          {
            id: "resetPageButton",
            value: "none",
            onclick: (e) => {
              e.preventDefault();
              e.redraw = false;
              console.log("TODO: Faire un truc gramload currentpagenumber -= 1")
            }
          },
          "Page Précédente (pas encore implémenté mais pas trop difficile)"
        ),
        m(
          "button",
          {
            id: "resetPageButton",
            value: "none",
            onclick: (e) => {
              e.preventDefault();
              e.redraw = false;
              console.log("TODO: vider les listes")
            }
          },
          "Retour au début (pas sûr qu'on en ait besoin)"
        ),
        m(
          "h2",
          {
            id: "pageNumberCounter",
          },
          "Numéro de la page : " + GramView.currentPageNumber
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
              if (GramView.filter == "All") {
                console.log(GramView.currentPageNumber);
                console.log(GramView.pageNumberAll);
                GramView.currentCursorList = GramView.cursorListAll;
                GramView.currentPageNumber = GramView.pageNumberAll;
              } else {
                GramView.currentCursorList = GramView.cursorListFollow;
                GramView.currentPageNumber = GramView.pageNumberFollow;
              }
              //On change de mode, donc changer la liste de curseurs, ainsi que le numero de page
              //Cependant, ne pas incrementer ces valeurs.
              Gram.loadList(GramView.filter, GramView.currentCursorList[GramView.currentPageNumber-1]).then(function() {
                if (GramView.filter == "All") {
                  GramView.currentCursorList[GramView.currentPageNumber] = Gram.cursor;
                  GramView.cursorListAll[GramView.currentPageNumber] = Gram.cursor;
                } else {
                  GramView.currentCursorList[GramView.currentPageNumber] = Gram.cursor;
                  GramView.cursorListFollow[GramView.currentPageNumber] = Gram.cursor;
                }
                document.getElementById("nextPageButton").value = Gram.cursor;
              })
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
                          "h2",
                          {
                            class: "ownerContainer",
                          },
                          postData.authorName
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
                                  Authorization:
                                    "Bearer " +
                                    GoogleAuth.currentUser
                                      .get()
                                      .getAuthResponse().id_token,
                                },
                                params: {
                                  postId: e.target.value,
                                },
                              }).then((res) => {
                                console.log(res);
                                if (res.error === undefined) {
                                  postData.likeCounter += 1;
                                  e.target.innerText =
                                    "Unlike"; /* Mensonger pour le moment */
                                }
                              });
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
