//TODO: replace these var by class equivalents?
var Profile = {
    view: function(vnode) {
        if (vnode.attrs.authStatus === undefined || !vnode.attrs.authStatus ) {
            return(m("div", [
                m("h1", {
                    id: "profileHeader"
                }, "In memoriam : Groquik"),
                m("p", {
                    id: "userName"
                }, "Nom : groquik"),
                m("p", {
                    id: "userInfo"
                }, "Vous voyez cette page pour indiquer que vous n'êtes pas connecté."),
                m("img", {
                    id: "userImage",
                    src: "https://nordpresse.be/wp-content/uploads/2020/10/final.jpg",
                    alt: "Repose en paix, Groquik"
                })
            ]))
            
        }
        else {
            return(m("div", [
                m("h1", {
                    id: "profileHeader"
                }, "Déconnectez-vous pour une surprise"),
                m("p", {
                    id: "userName"
                }, "Nom : " + vnode.attrs.userName),
                m("img", {
                    id: "userImage",
                    src: vnode.attrs.userImage,
                    alt: "Image utilisateur google"
                }),
                m("p", {
                    id: "dontdothat"
                }, "id_token KEEP THAT ONE SECURE : " + vnode.attrs.userToken),
            ]))
        }
    }
}

export default Profile;