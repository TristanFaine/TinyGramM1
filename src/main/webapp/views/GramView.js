//Logic, allow a "view new" or "view subbed" onupdate i guess?
import Gram from "../models/Gram.js";
var GramView = {
    filter: "",
    onupdate: function(vnode) {
        document.getElementById("postViewHeader").innerHTML = ("Filtre utilisé : " + GramView.filter);
        console.log(GramView.filter)
            var itemList = document.getElementById("PostList")
            while (itemList.firstChild) {
                itemList.removeChild(itemList.lastChild);
              }
            for (var i=0 ; i<Gram.list.length; i++) {
                //TODO: faire un look cool pour les composants
                var postContainer = document.createElement("div");
                postContainer.setAttribute("class", "postContainer");
                
                var imageContainer = document.createElement("img");
                imageContainer.setAttribute("class", "imageContainer");
                imageContainer.setAttribute("src", Gram.list[i].imageURL)

                var descriptionContainer = document.createElement("div");
                descriptionContainer.setAttribute("class", "descriptionContainer");
                descriptionContainer.appendChild(document.createTextNode(Gram.list[i].description));
                
                var likeContainer = document.createElement("div");
                likeContainer.setAttribute("class", "likeContainer");
                likeContainer.appendChild(document.createTextNode("like : TODO"));

                //todo, how to get user name from the userid?
                //we could do an api request.. but doing an api request for each post sounds overkill
                //Mettre un index supplémentaire "Owner|Nom" dans post risque de réduire les performances..?
                var ownerContainer = document.createElement("div");
                ownerContainer.setAttribute("class", "ownerContainerContainer");
                ownerContainer.appendChild(document.createTextNode("Posted by : " + Gram.list[i].imageURL));

                postContainer.appendChild(imageContainer);
                postContainer.appendChild(descriptionContainer);
                postContainer.appendChild(likeContainer);
                itemList.appendChild(postContainer);
            }
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
        }
        else {
            return (m("div", [
                m("h1", {
                    id: "postViewHeader"
                }, "Veuillez sélectionner un filtre pour voir les grams des autres :)"),
                m("p", {
                    id: "temp"
                }, "dsl je suis pas designer graphique"),
                m("button", {
                    class: "filterButton",
                    onclick: e => {
                        e.preventDefault();
                        e.redraw = false; //wait for events to finish before loading
                        //Ne pas autoriser à relancer l'appel
                        if (GramView.filter != "All") {
                            GramView.filter = "All";
                            Gram.loadList(GramView.filter);
                        }
                    }
                },"Nouveaux posts"),
                m("button", {
                    class: "filterButton",
                    onclick: e => {
                        e.preventDefault();
                        e.redraw = false; //wait for events to finish before loading
                        //Ne pas autoriser à relancer l'appel
                        if (GramView.filter != "SubbedOnly") {
                            GramView.filter = "SubbedOnly";
                            Gram.loadList(GramView.filter);
                        }
                    }
                },"Posts de ceux qu'on suit"),
                m("div#PostView", [
                    m("div#PostList")
                ])
            ]))
        }
    }
}

export default GramView;