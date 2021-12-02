var Subscriptions = {
    oncreate: function (vnode) {
        m.request({
          method: "GET",
          url: "/_ah/api/api/v1/users",
        }).then((x) => console.log(x));
    },
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
        return m("h1", "Ça marche");
      }
    },
  };
  export default Subscriptions;
  