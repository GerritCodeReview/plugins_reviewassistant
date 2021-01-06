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
package com.github.reviewassistant.reviewassistant.models;

/**
 * A class that represents a ReviewAssistant calculation. The class contains review time and review
 * session suggestions.
 */
public class Calculation {
  public String commitId;
  public int totalReviewTime;
  public int hours;
  public int minutes;
  public int sessions;

  public Calculation() {
    this.commitId = "nothing";
    this.totalReviewTime = 0;
    this.hours = 0;
    this.minutes = 0;
    this.sessions = 0;
  }

  public Calculation(String commitId, int totalReviewTime, int hours, int minutes, int sessions) {
    this.commitId = commitId;
    this.totalReviewTime = totalReviewTime;
    this.hours = hours;
    this.minutes = minutes;
    this.sessions = sessions;
  }
}
