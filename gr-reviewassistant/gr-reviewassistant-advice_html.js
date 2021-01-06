/**
 * @license
 * Copyright (c) 2019-2020 Francois Ferrand.
 * 
 * This code is licensed under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
export const htmlTemplate = Polymer.html`
    <style include="gr-change-metadata-shared-styles"></style>
    <style include="shared-styles">
      .review-advice {
        max-width: 20em;
        display: block;
        padding-left: 1.5em;
        padding-right: 0.5em;
      }
    </style>
    <template is="dom-if" if="[[_isVisible(change, revision)]]">
      <div class="separatedSection gr-change-metadata">
        <section class="gr-change-metadata">
          <span class="title gr-change-metadata">ReviewAssistant</span>
        </section>
        <span class="review-advice">
          <iron-icon src="/plugins/reviewassistant/static/loading.gif" hidden="[[!loading]]">
          </iron-icon>
          <gr-reviewassistant-htmllabel class="gr-change-metadata" hidden="[[loading]]" content="[[advice]]" >
          </gr-reviewassistant-htmllabel>
        </span>
      </div>
    </template>
`;
