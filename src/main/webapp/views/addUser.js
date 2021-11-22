//TODO: replace these var by class equivalents?
var AddUser = {
    view: function () {
        return m("div", [
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

export default AddUser;