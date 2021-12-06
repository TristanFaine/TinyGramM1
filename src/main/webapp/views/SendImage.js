function readFile() {
  var input = document.getElementById("imageInput");
  var description = document.getElementById("description");
  if (input.files && input.files[0]) {
    var FR = new FileReader();
    FR.addEventListener("load", (e) => {
      //Il ne faut PAS spécifier header:content-type, sinon il ne set pas de boundary automatiquement
      //et donc erreur "org.apache.commons.fileupload.FileUploadException: the request was rejected because no multipart boundary was found"
      SendImage.status = "Envoi en cours...";
      m.request({
        method: "POST",
        url: "/_ah/api/api/v1/addPost",
        headers: {
          Authorization:
            "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token,
        },
        params: {
          description: description.value,
        },
        body: { imageString: e.target.result },
      }).then((data) => {
        if (data.result) {
          SendImage.status = "✔️ Publication envoyée";
          input.value = description.value = "";
        }
        document.getElementById("imagePreviewBack").src = data.result;
      });
    });
    FR.readAsDataURL(input.files[0]);
  }
}
var SendImage = {
  status: "",
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
    } else {
      return m("div", [
        m(
          "form",
          {
            id: "imageForm",
            onsubmit: (e) => {
              e.preventDefault();
              readFile();
            },
          },
          [
            m(
              "h2",
              {
                class: "formtitle",
              },
              "Poster une image"
            ),
            m("input", {
              id: "imageInput",
              type: "file",
              name: "imageInput",
              accept: "image/png, image/jpeg",
              title: "Choisissez votre image ou déposez-la ici !",
            }),
            m(
              "label",
              {
                for: "description",
              },
              "Description"
            ),
            m("textarea", {
              id: "description",
              name: "description",
              placeholder: "Quoi de neuf en cette belle journée ?",
            }),
            m("input", {
              type: "submit",
            }),
          ]
        ),
        m("img", {
          id: "imagePreviewBack",
        }),
        m(
          "p",
          {
            id: "reponse",
          },
          SendImage.status
        ),
      ]);
    }
  },
};
export default SendImage;
