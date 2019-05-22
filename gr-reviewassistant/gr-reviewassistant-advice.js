(function() {
  'use strict';

  Polymer({
    is: 'gr-reviewassistant-advice',

    properties: {
      loading: Boolean,
      advice: String,
    },

    attached() {
      this.loading = true;
      var url = "/changes/" + this.change._number + "/revisions/" + this.revision._number + "/reviewassistant~advice";
      this.plugin.restApi().get(url).then( (resp) => {
        this.advice = resp;
        this.loading = false;
      });
    },

    _isVisible(change, revision) {
      return !((change.status != 'NEW') || (change.current_revision != revision.commit.commit));
    },
  });
})();
