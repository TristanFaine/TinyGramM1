//TODO: replace these var by class equivalents?
var MockProfile = {
    //user.getBasicProfile.getName()

    oninit() {
        console.log("je suis dans oninit")
    },
    view: function(vnode) {
        if (vnode.attrs.auth) {
            console.log(vnode.attrs.auth)
            console.log("poggers");
        }
        else {
            console.log(vnode.attrs.auth)
            //^ this is undefined in the console if nothing was set
            //therefore, it defaults to this.
            return(m("div", [
                m("h1", {
                    id: "profileHeader"
                }, "Afficher un truc vide ou faire redirection si user non authentifié"),
                m("p", {
                    id: "userName"
                }, "Nom : groquik"),
                m("p", {
                    id: "userInfo"
                }, "Info : groquik est gros, jaune, et il n'a pas encore d'adresse google")
            ]))
        }
    }
}

//Solution possible brute :
//Laisser cette vue s'occuper de la vue
//et laisser le "controlleur" faire le controle, aka le script d'avant.
//Idée : quand on clique sur le sign in, peut-etre faire un script : "based on current route/pageURL, refresh again, but sending auth, userinfo if needed, as parameters.

export default MockProfile;