import Gram from "../models/Gram.js";
var GramView = {
    filter: "None",
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
                }, "Filtre utilisé : " + GramView.filter),
                m("p", {
                    id: "temp"
                }, "dsl je suis pas designer graphique"),
                m("button", {
                    class: "filterButton",
                    onclick: e => {
                        e.preventDefault();
                        e.redraw = false; //wait for events to finish before loading
                        //Ne pas autoriser à relancer la même action
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
                        //Ne pas autoriser à relancer la même action
                        if (GramView.filter != "SubbedOnly") {
                            GramView.filter = "SubbedOnly";
                            Gram.loadList(GramView.filter);
                        }
                    }
                },"Posts de ceux qu'on suit"),
                m("div#PostView", [
                    m("div#PostList", Gram.list.map(function(postData) {
                        return m("div", {
                            class: "postContainer"
                        }, [
                            m("img", {
                                class: "imageContainer",
                                src: postData.imageURL
                            }),
                            m("div", {
                                class: "descriptionContainer"
                            }, postData.description),
                            m("div", {
                                class: "likeContainer"
                            }, postData.likeCounter + " likes"),
                            m("div", {
                                class: "ownerContainer"
                            }, "Posted by : " + postData.userId)
                        ])
                    }))
                ])
            ]))
        }
    }
}

export default GramView;