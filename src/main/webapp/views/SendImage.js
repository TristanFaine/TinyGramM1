function readFile() {
  var input = document.getElementById("imageInput");
  if (input.files && input.files[0]) {
    var FR = new FileReader();
    FR.addEventListener("load", (e) => {
      document.getElementById("imagePreview").src = e.target.result;
      var fileType = e.target.result.split(",")[0].split("/")[1].split(";")[0];

      //Il ne faut PAS spécifier header:content-type, sinon il ne set pas de boundary automatiquement
      //et donc erreur "org.apache.commons.fileupload.FileUploadException: the request was rejected because no multipart boundary was found"
      if (fileType == "png" || fileType == "jpeg") {
        m.request({
          method: "POST",
          url: "/_ah/api/api/v1/addImage",
          headers: {
            Authorization:
              "Bearer " +
              GoogleAuth.currentUser.get().getAuthResponse().id_token,
          },
          params: {
            description: document.getElementById("description").value,
          },
          body: { imageString: e.target.result },
        }).then((data) => {
          m.render(
            document.getElementById("reponse"),
            //.result est un nom qu'on a donné au paramétre de réponse principale,
            `Reception de réponse ${data.result}`
          );
          document.getElementById("imagePreviewBack").src = data.result;
        });
      } else {
        document.getElementById("reponse").innerHTML =
          "Cette application ne prends en compte que les fichiers jpeg ou png.";
      }
    });
    FR.readAsDataURL(input.files[0]);
  }
}
var SendImage = {
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
              title: "Choisissez votre image ou déposez-la ici !"
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
              placeholder: "Quoi de neuf en cette belle journée ?"
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
          ""
        ),
      ]);
    }
  },
};
export default SendImage;
