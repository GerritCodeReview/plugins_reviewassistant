if (window.Polymer) {
    return;
}
Gerrit.install(function(self) {
    function print (c, r) {
        if ((c.status != 'NEW') || (c.current_revision != r.commit.commit)) {
            return;
        }

        var container = document.createElement('div');
        container.id = "reviewAssistant";
        container.style = "padding-top: 10px;";
        var header = document.createElement('div');
        header.innerHTML = "<strong>ReviewAssistant</strong>";
        container.appendChild(header);
        var advice = document.createElement('div');
        advice.innerHtml = "<p><img src=\"plugins/reviewassistant/static/loading.gif\"></p>";
        container.appendChild(advice);

        var change_plugins = document.getElementById('change_plugins');
        change_plugins.appendChild(container);

        var url = "/changes/" + c._number + "/revisions/" + r._number + "/reviewassistant~advice";
        Gerrit.get(url, function (r) {
            advice.innerHTML = r;
        });
    }

    self.on('showchange', print);
});
