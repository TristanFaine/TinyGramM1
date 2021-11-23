//TODO: au lieu d'un seul input pour l'image, faire un form enctype="multipart/form-data"
//pour envoyer un petit message en plus de l'image :)
//changer donc l'event listener en conséquence.
function readFile() {
    if(this.files && this.files[0]) {
        var FR = new FileReader();
        FR.addEventListener("load", function(e) {
            console.log(e.target.result);
            document.getElementById("imagePreview").src = e.target.result;

            //utiliser la fonction prédéfinie sendAuthorizedApiRequest(requestDetails)?
            //c'est juste un ifauthorized => do thing, else force sign in, then retry
            //Docs utile :
            //https://developer.mozilla.org/fr/docs/Web/HTTP/Headers/Authorization
            //https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
            //https://stackoverflow.com/questions/2422468/how-can-i-upload-files-to-a-server-using-jsp-servlet/2424824#2424824

            //TODO: Check if data is actually an image before sending...
            //maybe restrict input to .jpg and/or .png
            var fd = new FormData();
            fd.append("userImage", e.target.result);
            fd.append("userName", GoogleAuth.currentUser.get().getBasicProfile().getName());
            fd.append("description", "remplacer le dom input par un form :)")
            //fd.append("description", document.getElementById("X").innerHTML)
            
            //Il ne faut PAS spécifier header:content-type, sinon il ne set pas de boundary automatiquement
            // et donc erreur "org.apache.commons.fileupload.FileUploadException: the request was rejected because no multipart boundary was found"
            m.request({
                method: "POST",
                url: '/sendImage',
                headers: {
                    "Authorization": "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token
                },
                body: fd
            }).then(data => {
                m.render(document.getElementById("reponse"),
                    `Vous avez donné le paramétre ${data}`)
            })

        });
    FR.readAsDataURL(this.files[0]);
    }
}


var MockImage = {
    view: function(vnode) {
        if (vnode.attrs.authStatus === undefined || !vnode.attrs.authStatus ) {
            return(m("div", [
                m("h1", {
                    id: "userNotConnectedHeader"
                }, "Veuillez vous connecter pour accéder à ce service.")
            ]))
            
        }
        else {
            return(m("div", [
                m("h1", {
                    id: "profileHeader"
                }, "Essayez d'envoyer une image et elle devrait s'afficher.."),      
                m("input", {
                    id: "imageInput", 
                    onchange: readFile,
                    type: "file"
                }),
                m("img", {
                    id: "imagePreview",
                }),
                m("p", {
                    id: "reponse",
                },"pogU?"),
                m("p", {
                    id: "imageB64Preview",
                },"Rien ici pour l'instant")
            ]))
        }
    }
}
export default MockImage;
                
