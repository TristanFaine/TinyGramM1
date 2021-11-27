var UnknownProfile = {
    view: function (vnode) {
        return (m("div", [
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
}

export default UnknownProfile;