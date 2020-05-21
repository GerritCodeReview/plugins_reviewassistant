import './gr-reviewassistant-htmllabel.js';
import {htmlTemplate} from './gr-reviewassistant-advice_html.js';

class GrReviewAssistantAdvice extends Polymer.Element {
  static get is() { return 'gr-reviewassistant-advice'; }

  static get properties() {
    return {
      loading: Boolean,
      advice: String,
    };
  }

  connectedCallback() {
    super.connectedCallback();
    this.loading = true;
    const actionId = this.plugin.getPluginName() + '~advice';
    const url = '/changes/' + this.change._number + '/revisions/' +
                this.revision._number + '/' + actionId;
    this.plugin.restApi().get(url).then( resp => {
      this.advice = resp;
      this.loading = false;
    });
  }

  _isVisible(change, revision) {
    return !((change.status != 'NEW') ||
           (change.current_revision != revision.commit.commit));
  }
}

customElements.define(GrReviewAssistantAdvice.is, GrReviewAssistantAdvice);
