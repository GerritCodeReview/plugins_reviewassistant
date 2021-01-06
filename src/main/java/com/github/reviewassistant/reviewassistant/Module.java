/**
 * Copyright (c) 2014-2015 Gustav Jansson Ekstrand (gustav.jp@live.se), Simon Wessel (simon.w.karlsson@gmail.com),
 * William Phan (william.da.phan@gmail.com), Sony Mobile Communications Inc.
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
package com.github.reviewassistant.reviewassistant;

import static com.google.gerrit.server.change.RevisionResource.REVISION_KIND;

import com.github.reviewassistant.reviewassistant.rest.GetAdvice;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.restapi.RestApiModule;
import com.google.gerrit.server.events.EventListener;

public class Module extends FactoryModule {

  @Override
  protected void configure() {
    DynamicSet.bind(binder(), EventListener.class).to(ChangeEventListener.class);
    bind(AdviceCache.class).to(AdviceCacheImpl.class);
    install(AdviceCacheImpl.module());
    factory(ReviewAssistant.Factory.class);

    install(
        new RestApiModule() {
          @Override
          protected void configure() {
            get(REVISION_KIND, "advice").to(GetAdvice.class);
          }
        });
  }
}
