import {htmlTemplate} from './gr-reviewassistant-htmllabel_html.js';

class GrReviewAssistantHtmlLabel extends Polymer.Element {
  static get is() { return 'gr-reviewassistant-htmllabel'; }

  static get template() { return htmlTemplate; }

  static get properties() {
    return {
      content: {
        type: String,
        observer: '_contentChanged',
      },
    };
  }

  ready() {
    super.ready();
    this.$.label.innerHTML = this.content;
  }

  _contentChanged() {
    this.$.label.innerHTML = this.content;
  }
}

customElements.define(GrReviewAssistantHtmlLabel.is,
    GrReviewAssistantHtmlLabel);
