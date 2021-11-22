function readFile() {
    if(this.files && this.files[0]) {
        var FR = new FileReader();
        FR.addEventListener("load", function(e) {
            console.log(e.target.result);
            document.getElementById("imagePreview").src = e.target.result;
            document.getElementById("imageB64Preview").innerHTML = e.target.result;
            //TODO: faire requete api ici, ou depuis controlleur avec notre truc blob
            //en verifiant que celui-ci est valide evidemment...
            //apparament l'API FormData est approprié
            // https://developer.mozilla.org/en-US/docs/Web/API/FormData
            //ah bah fallait juste lire la doc mithril https://mithril.js.org/request.html section file uploads

            /*
            var fd = new FormData();
            fd.append("userImage", e.target.result)

            //TODO:
            //create blob -> send blob -> read blob -> put things in datastore | cloud storage
            //but yeah since we need the id token we're gonna have to either find a way to view
            //the user object from here, or do the function from the controller.
            //or just pass the token as a ... wait no that's not a good idea
            //using the sendAuthorizedApiRequest implies being able to read the gauth token thing i suppose
            

            //this can read SCOPE so it should (probably) be able to read Googleauth...
            
            //values in blob :
            // l'image
            // le id.token de la personne
            // autres infos

            //un truc du genre Blob {type "x", size: x, slice: function}
            //Puis poster ce blob vers servlet (temporaire)
            //et voir si ça passe
            m.request({
                method: "POST",
                url: '/sendImage',
                headers: {

                },
                body: fd
            })
            */
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
                    id: "imageB64Preview",
                },"Rien ici pour l'instant"),
                
            ]))
        }
    }
}
export default MockImage;