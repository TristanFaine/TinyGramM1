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
                Gram.list.push({imageURL: entities[i].properties.imageURL, description: entities[i].properties.description,
                     userId: entities[i].properties.userId, likeCounter: entities[i].properties.likeCounter})
            }
        })
    }
}

export default Gram;