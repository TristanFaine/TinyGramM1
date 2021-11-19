var Splash = {
    view: function () {
        return m("div", [
            m("h1", {
                class: "title"
            }, "Tinygram"),
            m("div", [
                m("a", {
                    href: "#!/AddUser"
                }, "Ajouter un utilisateur"),
                m("hr"),
                m("a", {
                    href: "#!/MockProfile"
                }, "DEBUG: Affichage infos Google")
            ])
        ])
    }
}
//TODO: ^ manually adding an a href for every new page is a pain. maybe figure something
//to do it automatically.


//TODO: voir si on fait import x from x; var y = x(options)
//Ou si on utilise des vnodes : m.route.set("/MockProfile", {auth: false})
import MockProfile from '/views/MockProfile.js';
import AddUser from '/views/AddUser.js';

var app = document.getElementById("app")
m.route(app, "/splash", {
    "/splash": Splash,
    "/AddUser": AddUser,
    "/MockProfile": MockProfile
})

