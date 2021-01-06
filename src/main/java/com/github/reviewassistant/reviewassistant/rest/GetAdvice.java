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
package com.github.reviewassistant.reviewassistant.rest;

import com.github.reviewassistant.reviewassistant.AdviceCache;
import com.github.reviewassistant.reviewassistant.models.Calculation;
import com.google.gerrit.extensions.restapi.Response;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.change.RevisionResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This rest view fetches a calculation and returns it. It is used by the front-end to present the
 * review suggestions to the users.
 */
@Singleton
public class GetAdvice implements RestReadView<RevisionResource> {

  private AdviceCache adviceCache;

  @Inject
  public GetAdvice(AdviceCache adviceCache) {
    this.adviceCache = adviceCache;
  }

  @Override
  public Response<String> apply(RevisionResource resource) throws RestApiException {
    Calculation calculation = adviceCache.fetchCalculation(resource);
    if (calculation == null) {
      return Response.ok("Could not get advice for this change.");
    }
    StringBuilder advice = new StringBuilder("Reviewers should spend <strong>");
    if (calculation.hours >= 1) {
      advice.append(calculation.hours).append(" hour").append(calculation.hours > 1 ? "s" : "");
    }
    if (calculation.hours > 0 && calculation.minutes > 0) {
      advice.append(" and ");
    }
    if (calculation.minutes > 0) {
      advice
          .append(calculation.minutes)
          .append(" minute")
          .append(calculation.minutes > 1 ? "s" : "");
    }
    advice.append("</strong> reviewing this change.");
    if (calculation.hours >= 1) {
      advice
          .append("<p>This should be split up in <strong>")
          .append(calculation.sessions)
          .append(" to ")
          .append(calculation.sessions + 1)
          .append(" sessions</strong>.");
    }

    return Response.ok(advice.toString());
  }
}
