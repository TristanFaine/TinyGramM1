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
      console.log(data.items);
      this.loaded = true;
      this.list = data.items.map((entity) => ({...entity.properties, key: entity.key.name}))
      console.log(this.list);
    });
  },
};

export default Gram;
