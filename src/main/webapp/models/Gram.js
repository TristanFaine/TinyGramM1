var Gram = {
  list: [],
  cursor: "",
  loaded: false,
  limitReached: false,
  loadList: function (f,c) {
    return new Promise(function(resolve,reject) {Gram.loaded = false; // Sans effet sur l’affichage...
      m.request({
        method: "GET",
        url: "/_ah/api/api/v1/getPosts",
        headers: {
          Authorization:
            "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token,
        },
        params: {
          filter: f,
          cursor: c
        },
      }).then((data) => {
        Gram.loaded = true;
        //TODO: Faire quelque chose quand data.entities est vide AKA on a atteint la limite des posts donc limitReached = true
        //Note: entities est un nom customisé, le nom de l'array par défaut est "Items"
        Gram.list = data.entities.map((entity) => ({...entity.properties, key: entity.key.name}))
        console.log(Gram.list);
        Gram.cursor = data.cursor;
        resolve();
      });
      
    })
  },
};

export default Gram;
