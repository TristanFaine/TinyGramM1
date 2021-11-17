var m = require("mithril")
var AddUser = {
    view: function () {
        return m("main", [
            m("h1", {
                class: "title"
            }, "Tinygram"),
            m("form", {
                id: "inscription", onsubmit: e => {
                    e.preventDefault()
                    m.request({
                        method: 'POST',
                        url: '/addPpl',
                        params: {
                            id: document.getElementById('id').value
                        }
                    }).then(data => {
                        m.render(document.getElementById("reponse"),
                            `Vous êtes enregistré avec l’adresse ${data}`)
                    })
                } 
            }, [
        m("h2", {
            class: "formtitle"
        }, "S'inscrire"),
        m("label", {
            for: "id"
        }, "Adresse"),
        m("input", {
            id: "id",
            type: "text",
            name: "id",
            value: ""
        }),
        m("input", {
            type: "submit",
            name: "C'est parti",
        })
    ]),
    m("div", {
        id: "reponse"
            }, "Pas encore de reponse...")
        ])
    }
}