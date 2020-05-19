package com.github.reviewassistant.reviewassistant;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.PluginUser;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.data.ChangeAttribute;
import com.google.gerrit.server.data.PatchSetAttribute;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.WorkQueue;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.util.RequestContext;
import com.google.gerrit.server.util.ThreadLocalRequestContext;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import java.io.IOException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The change event listener listens to new commits and passes them on to the algorithm. */
class ChangeEventListener implements EventListener {

  private static final Logger log = LoggerFactory.getLogger(ChangeEventListener.class);
  private final ReviewAssistant.Factory reviewAssistantFactory;
  private final ThreadLocalRequestContext tl;
  private final PluginUser pluginUser;
  private final IdentifiedUser.GenericFactory identifiedUserFactory;
  private final PluginConfigFactory cfg;
  private final ChangeData.Factory changeDataFactory;
  private final String pluginName;
  private WorkQueue workQueue;
  private GitRepositoryManager repoManager;

  @Inject
  ChangeEventListener(
      final ReviewAssistant.Factory reviewAssistantFactory,
      final WorkQueue workQueue,
      final GitRepositoryManager repoManager,
      final ThreadLocalRequestContext tl,
      final PluginUser pluginUser,
      final IdentifiedUser.GenericFactory identifiedUserFactory,
      final PluginConfigFactory cfg,
      final ChangeData.Factory changeDataFactory,
      @PluginName String pluginName) {
    this.workQueue = workQueue;
    this.reviewAssistantFactory = reviewAssistantFactory;
    this.repoManager = repoManager;
    this.tl = tl;
    this.pluginUser = pluginUser;
    this.identifiedUserFactory = identifiedUserFactory;
    this.cfg = cfg;
    this.changeDataFactory = changeDataFactory;
    this.pluginName = pluginName;
  }

  @Override
  public void onEvent(Event changeEvent) {
    if (!(changeEvent instanceof PatchSetCreatedEvent)) {
      return;
    }
    PatchSetCreatedEvent event = (PatchSetCreatedEvent) changeEvent;
    ChangeAttribute c = event.change.get();
    PatchSetAttribute p = event.patchSet.get();
    log.debug("Received new commit: {}", p.revision);

    Project.NameKey projectName = event.getProjectNameKey();

    boolean autoAddReviewers = true;
    try {
      log.debug("Checking if autoAddReviewers is enabled");
      autoAddReviewers =
          cfg.getProjectPluginConfigWithInheritance(projectName, pluginName)
              .getBoolean("reviewers", "autoAddReviewers", true);
    } catch (NoSuchProjectException e) {
      log.error("Could not find project {}", projectName);
    }
    log.debug(autoAddReviewers ? "autoAddReviewers is enabled" : "autoAddReviewers is disabled");
    if (autoAddReviewers) {
      try (Repository repo = repoManager.openRepository(projectName);
           RevWalk walk = new RevWalk(repo)) {
        Change.Id changeId = new Change.Id(c.number);
        final ChangeData cd = changeDataFactory.create(projectName, changeId);
        if (cd == null) {
          log.warn("Could not find change {} in project {}", changeId.get(),
               projectName.toString());
          return;
        }

        final Change change = cd.change();
        PatchSet.Id psId = new PatchSet.Id(changeId, p.number);
        PatchSet ps = cd.patchSet(psId);
        if (ps == null) {
          log.warn("Could not find patch set {}", psId.get());
          return;
        }

        RevCommit commit = walk.parseCommit(ObjectId.fromString(p.revision));

        final Runnable task =
            reviewAssistantFactory.create(commit, change, ps, repo, projectName);
        workQueue
            .getDefaultQueue()
            .submit(
                new Runnable() {
                  @Override
                  public void run() {
                    RequestContext old =
                        tl.setContext(
                            new RequestContext() {
                              @Override
                              public CurrentUser getUser() {
                                if (!ReviewAssistant.realUser) {
                                  return pluginUser;
                                }
                                return identifiedUserFactory.create(change.getOwner());
                              }
                            });
                    try {
                      task.run();
                    } finally {
                      tl.setContext(old);
                    }
                  }
                });
      } catch (OrmException | IOException x) {
        log.error(x.getMessage(), x);
      }
    }
  }
}
