import './gr-reviewassistant-advice.js';

Gerrit.install(plugin => {
  plugin.registerCustomComponent(
      'change-metadata-item', 'gr-reviewassistant-advice');
});