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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.PatchSet;
import com.google.gerrit.entities.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.PluginUser;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.data.ChangeAttribute;
import com.google.gerrit.server.data.PatchSetAttribute;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.EventListener;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import com.google.gerrit.server.events.PatchSetEvent;
import com.google.gerrit.server.events.PrivateStateChangedEvent;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.WorkQueue;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.util.RequestContext;
import com.google.gerrit.server.util.ThreadLocalRequestContext;
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

  private boolean isUnmarkPrivateEvent(Event changeEvent) {
    if (!(changeEvent instanceof PrivateStateChangedEvent)) {
      return false;
    }
    PatchSetEvent event = (PrivateStateChangedEvent) changeEvent;
    return !Boolean.TRUE.equals(event.change.get().isPrivate);
  }

  @Override
  public void onEvent(Event changeEvent) {
    boolean unmarkPrivate = isUnmarkPrivateEvent(changeEvent);
    if (!(changeEvent instanceof PatchSetCreatedEvent) && !unmarkPrivate) {
      return;
    }
    PatchSetEvent event = (PatchSetEvent) changeEvent;
    ChangeAttribute c = event.change.get();
    PatchSetAttribute p = event.patchSet.get();
    log.debug(unmarkPrivate ? "Unmark private commit {}" : "Received new commit: {}", p.revision);

    Project.NameKey projectName = event.getProjectNameKey();

    boolean autoAddReviewers = true;
    boolean ignorePrivate = true;
    boolean ignoreWip = false;
    try {
      log.debug("Checking if autoAddReviewers is enabled");
      autoAddReviewers =
          cfg.getProjectPluginConfigWithInheritance(projectName, pluginName)
              .getBoolean("reviewers", "autoAddReviewers", autoAddReviewers);
      ignorePrivate =
          cfg.getProjectPluginConfigWithInheritance(projectName, pluginName)
              .getBoolean("reviewers", "ignorePrivate", ignorePrivate);
      ignoreWip =
          cfg.getProjectPluginConfigWithInheritance(projectName, pluginName)
              .getBoolean("reviewers", "ignoreWip", ignoreWip);
    } catch (NoSuchProjectException e) {
      log.error("Could not find project {}", projectName);
    }

    if ((!ignorePrivate && unmarkPrivate)
        || (ignorePrivate && Boolean.TRUE.equals(c.isPrivate))
        || (ignoreWip && Boolean.TRUE.equals(c.wip))) {
      return;
    }

    log.debug(autoAddReviewers ? "autoAddReviewers is enabled" : "autoAddReviewers is disabled");
    if (autoAddReviewers) {
      try (Repository repo = repoManager.openRepository(projectName);
           RevWalk walk = new RevWalk(repo)) {
        Change.Id changeId = Change.id(c.number);
        final ChangeData cd = changeDataFactory.create(projectName, changeId);
        if (cd == null) {
          log.warn(
              "Could not find change {} in project {}", changeId.get(), projectName.toString());
          return;
        }

        final Change change = cd.change();
        PatchSet.Id psId = PatchSet.id(changeId, p.number);
        PatchSet ps = cd.patchSet(psId);
        if (ps == null) {
          log.warn("Could not find patch set {}", psId.get());
          return;
        }

        RevCommit commit = walk.parseCommit(ObjectId.fromString(p.revision));

        final Runnable task = reviewAssistantFactory.create(commit, change, ps, repo, projectName);
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
      } catch (IOException x) {
        log.error(x.getMessage(), x);
      }
    }
  }
}
