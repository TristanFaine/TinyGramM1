function readFile() {
    var input = document.getElementById("imageInput");
    if (input.files && input.files[0]) {
        var FR = new FileReader();
        FR.addEventListener("load", e => {
            document.getElementById("imagePreview").src = e.target.result;
            //utiliser la fonction prédéfinie sendAuthorizedApiRequest(requestDetails)?
            //c'est juste un ifauthorized => do thing, else force sign in, then retry
            //Docs utile :
            //https://developer.mozilla.org/fr/docs/Web/HTTP/Headers/Authorization
            //https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
            //https://stackoverflow.com/questions/2422468/how-can-i-upload-files-to-a-server-using-jsp-servlet/2424824#2424824

            //TODO: Check if data is actually an image before sending...
            //maybe restrict input to .jpg and/or .png

            //TODO: Envoyer objet json plutot qu'un formdata 

            var fd = new FormData();
            fd.append("userImage", e.target.result);
            fd.append("userName", GoogleAuth.currentUser.get().getBasicProfile().getName());
            fd.append("description", document.getElementById("description").innerHTML)

            //Il ne faut PAS spécifier header:content-type, sinon il ne set pas de boundary automatiquement
            //et donc erreur "org.apache.commons.fileupload.FileUploadException: the request was rejected because no multipart boundary was found"
            m.request({
                method: "POST",
                url: '/sendImage',
                headers: {
                    "Authorization": "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token
                },
                body: fd
            }).then(data => {
                m.render(document.getElementById("reponse"),
                    `Reception de réponse ${data}`);
                
                //Note : Si data n'est pas valide pour src, alors notre appli essaye d'utiliser
                //un do get sur la http://localhost:8080/REPONSE
                //je n'ai aucune idée si c'est quelque chose que fait js, java, la config, ou autre
                //Une solution simple est de ne pas insérer autre chose qu'une image.
                document.getElementById("imagePreviewBack").src = data;
            })

        });
        FR.readAsDataURL(input.files[0]);
    }
}



var SendImage = {
    view: function (vnode) {
        if (vnode.attrs.authStatus === undefined || !vnode.attrs.authStatus) {
            return (m("div", [
                m("h1", {
                    id: "userNotConnectedHeader"
                }, "Veuillez vous connecter pour accéder à ce service.")
            ]))

        }
        else {
            return (m("div", [
                m("form", {
                    id: "imageForm",
                    onsubmit: e => {
                        e.preventDefault()
                        readFile()
                    }
                }, [
                    m("h2", {
                        class: "formtitle"
                    }, "Essaye de poster une image"),
                    m("label", {
                        for: "description"
                    }, "Ecris du texte pour la description"),
                    m("hr"),
                    m("input", {
                        id: "description",
                        type: "text",
                        name: "description",
                        value: ""
                    }),
                    m("input", {
                        id: "imageInput",
                        type: "file",
                        name: "imageInput",
                    }),
                    m("input", {
                        type: "submit",
                    })
                ]),
                m("img", {
                    id: "imagePreview",
                }),
                m("hr"),
                m("img", {
                    id: "imagePreviewBack",
                }),
                m("p", {
                    id: "reponse",
                }, "")
            ]))
        }
    }
}
export default SendImage;
