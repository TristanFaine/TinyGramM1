var Gram = {
  list: [],
  loaded: false,
  loadList: function (f) {
    this.loaded = false; // Sans effet sur l’affichage...
    m.request({
      method: "GET",
      url: "/_ah/api/api/v1/getPosts",
      headers: {
        Authorization:
          "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token,
      },
      params: {
        filter: f,
      },
    }).then((data) => {
      this.loaded = true;
      this.list = data.items.map((entity) => entity.properties);
    });
  },
};

export default Gram;
