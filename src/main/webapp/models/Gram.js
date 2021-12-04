//TODO: just make a load function with 2 parameters, size and mode, to 
//call API
var Gram = {
    list: [],
    loadList: function(f) {
        m.request({
            method: 'GET',
            url: '/_ah/api/api/v1/getPosts',
            headers: {
                "Authorization": "Bearer " + GoogleAuth.currentUser.get().getAuthResponse().id_token
            },
            params: {
                filter: f,
            }
        }).then(data => {
            this.list = [];
            var entities = data.items;
            console.log(entities)
            for (var i=0 ; i<entities.length; i++) {
                Gram.list.push({ imageURL: entities[i].key.name, description: entities[i].properties.description,
                     userId: entities[i].key.name})
            }
        })
    }
}


export default Gram;