(function() {
  'use strict';

  Polymer({
    is: 'gr-reviewassistant-htmllabel',

    properties: {
      content: {
        type: String,
        observer: '_contentChanged'
      }
    },

    ready() {
      this.innerHTML = this.content;
    },

    _contentChanged() {
      this.innerHTML = this.content;
    },
  });
})();
