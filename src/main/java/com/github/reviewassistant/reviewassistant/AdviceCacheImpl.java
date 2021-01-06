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

import com.github.reviewassistant.reviewassistant.cache.proto.Reviewassistant.CalculationProto;
import com.github.reviewassistant.reviewassistant.models.Calculation;
import com.google.common.cache.Cache;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.proto.Protos;
import com.google.gerrit.server.cache.CacheModule;
import com.google.gerrit.server.cache.serialize.CacheSerializer;
import com.google.gerrit.server.cache.serialize.StringCacheSerializer;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of AdviceCache as a Gerrit CacheModule. */
@Singleton
public class AdviceCacheImpl implements AdviceCache {

  public static final String REVIEW_ADVICE = "review_advice";

  private static final Logger log = LoggerFactory.getLogger(AdviceCacheImpl.class);
  private final GerritApi gApi;
  private final PluginConfigFactory cfg;
  private final String pluginName;

  private final Cache<String, Calculation> cache;

  public static Module module() {
    return new CacheModule() {
      @Override
      protected void configure() {
        persist(REVIEW_ADVICE, String.class, Calculation.class)
            .version(1)
            .keySerializer(StringCacheSerializer.INSTANCE)
            .valueSerializer(new Serializer());
      }
    };
  }

  @Inject
  AdviceCacheImpl(
      GerritApi gApi,
      PluginConfigFactory cfg,
      @Named(REVIEW_ADVICE) Cache<String, Calculation> cache,
      @PluginName String pluginName) {
    this.gApi = gApi;
    this.cfg = cfg;
    this.cache = cache;
    this.pluginName = pluginName;
  }

  static class Serializer implements CacheSerializer<Calculation> {
    @Override
    public byte[] serialize(Calculation calculation) {
      return Protos.toByteArray(
          CalculationProto.newBuilder()
              .setCommitId(calculation.commitId)
              .setTotalReviewTime(calculation.totalReviewTime)
              .setHours(calculation.hours)
              .setMinutes(calculation.minutes)
              .setSessions(calculation.sessions)
              .build());
    }

    @Override
    public Calculation deserialize(byte[] in) {
      CalculationProto proto = Protos.parseUnchecked(CalculationProto.parser(), in);
      return new Calculation(
          proto.getCommitId(),
          proto.getTotalReviewTime(),
          proto.getHours(),
          proto.getMinutes(),
          proto.getSessions());
    }
  }

  @Override
  public Calculation fetchCalculation(RevisionResource resource) {
    String revision = resource.getPatchSet().commitId().name();
    try {
      Calculation calc =
          cache.get(
              revision,
              () -> {
                try {
                  ChangeApi cApi = gApi.changes().id(resource.getChange().getChangeId());
                  ChangeInfo info = cApi.get();
                  double reviewTimeModifier =
                      cfg.getProjectPluginConfigWithInheritance(
                              resource.getChange().getProject(), pluginName)
                          .getInt("time", "reviewTimeModifier", 100);
                  Calculation c = ReviewAssistant.calculate(info, reviewTimeModifier / 100);
                  return c;
                } catch (RestApiException e) {
                  log.error(
                      "Could not get ChangeInfo for change {}", resource.getChange().getChangeId());
                  throw e;
                } catch (NoSuchProjectException e) {
                  log.error(e.getMessage(), e);
                  throw e;
                }
              });
      if (calc == null || calc.totalReviewTime == 0) {
        log.debug("Corrupt or missing calculation for {}", revision);
        cache.invalidate(revision);
        return null;
      }
      return calc;
    } catch (Exception e) {
      return null;
    }
  }
}
