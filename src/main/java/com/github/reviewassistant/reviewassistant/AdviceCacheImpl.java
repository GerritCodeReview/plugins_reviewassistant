package com.github.reviewassistant.reviewassistant;

import com.github.reviewassistant.reviewassistant.models.Calculation;
import com.google.common.cache.Cache;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.server.cache.CacheModule;
import com.google.gerrit.server.cache.serialize.CacheSerializer;
import com.google.gerrit.server.cache.serialize.StringCacheSerializer;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gson.Gson;
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
      Gson gson = new Gson();
      String s = gson.toJson(calculation);
      return s.getBytes();
    }

    @Override
    public Calculation deserialize(byte[] in) {
      Gson gson = new Gson();
      return gson.fromJson(new String(in), Calculation.class);
    }
  }

  @Override
  public Calculation fetchCalculation(RevisionResource resource) {
    String revision = resource.getPatchSet().getRevision().get();
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
