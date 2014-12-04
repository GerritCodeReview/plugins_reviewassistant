package com.github.nexception.reviewassistant;

import com.github.nexception.reviewassistant.models.Calculation;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.Patch.ChangeType;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.server.events.PatchSetCreatedEvent;
import com.google.gerrit.server.patch.PatchList;
import com.google.gerrit.server.patch.PatchListCache;
import com.google.gerrit.server.patch.PatchListEntry;
import com.google.gerrit.server.patch.PatchListNotAvailableException;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for calculating recommended review time and
 * recommended reviewers.
 */
public class ReviewAssistant implements Runnable {

    private final Repository repo;
    private final PatchListCache patchListCache;
    private final Change change;
    private final PatchSet ps;
    private final RevCommit commit;

    private static final Logger log = LoggerFactory.getLogger(ReviewAssistant.class);

    public interface Factory {
        ReviewAssistant create(RevCommit commit, Change change, PatchSet ps, Repository repo);
    }

    @Inject
    public ReviewAssistant(final PatchListCache patchListCache,
                           @Assisted final RevCommit commit, @Assisted final Change change,
                           @Assisted final PatchSet ps, @Assisted final Repository repo) {
        this.commit = commit;
        this.change = change;
        this.ps = ps;
        this.patchListCache = patchListCache;
        this.repo = repo;
    }

    /**
     * Returns a Calculation object with all relevant information
     * regarding a review for a patch set.
     * @param event the event for a patch set
     * @return      the Calculation object for a review
     */
    public static Calculation calculate(PatchSetCreatedEvent event) {
        log.info("Received event: " + event.patchSet.revision);
        Calculation calculation = new Calculation();
        calculation.commitId = event.patchSet.revision;
        calculation.totalReviewTime = calculateReviewTime(event);
        calculation.hours = calculateReviewTime(event) / 60;
        calculation.minutes = calculateReviewTime(event) % 60;
        calculation.sessions = calculateReviewSessions(calculateReviewTime(event));
        calculation.sessionTime = 60;

        return calculation;
    }

    /**
     * Returns the total amount of time in minutes recommended for a review.
     * Adds all line insertions and deletions for a patch set and calculates
     * the amount of minutes needed.
     * This calculation is based on the optimum review rate of 5 LOC in 1 minute.
     * @param event the event for a patch set
     * @return      the total amount of time recommended for a review
     */
    private static int calculateReviewTime(PatchSetCreatedEvent event) {
        int lines = event.patchSet.sizeInsertions + Math.abs(event.patchSet.sizeDeletions);
        log.info("Insertions: " + event.patchSet.sizeInsertions);
        log.info("Deletions: " + event.patchSet.sizeDeletions);
        int minutes = (int) Math.ceil(lines / 5);
        if(minutes < 5) {
            minutes = 5;
        }
        return minutes;
    }

    /**
     * Returns the recommended amount of review sessions for a review.
     * Divides the total amount of review time up in 60 minute sessions.
     * @param minutes the total amount of time recommended for a review
     * @return        the recommended amount of review sessions
     */
    private static int calculateReviewSessions(int minutes) {
        int sessions = Math.round(minutes / 60);
        if (sessions < 1) {
            sessions = 1;
        }
        return sessions;
    }

    /**
     * Calculates blame data for a given file and commit.
     * @param commit the commit to base the blame command on
     * @param file the file for which to calculate blame data
     * @return BlameResult
     */
    private BlameResult calculateBlame(RevCommit commit, PatchListEntry file) {
        BlameCommand blameCommand = new BlameCommand(repo);
        blameCommand.setStartCommit(commit);
        blameCommand.setFilePath(file.getNewName());

        try {
            BlameResult blameResult = blameCommand.call();
            blameResult.computeAll();
            return blameResult;
        } catch (GitAPIException e) {
            log.error("Could not call blame command for commit {}", commit.getName(), e);
        } catch (IOException e) {
            log.error("Could not compute blame result for commit {}", commit.getName(), e);
        }
        return null;
    }

    @Override
    public void run() {
        PatchList patchList;
        //TODO: Store reviewers in this map.
        Map<Account, Integer> reviewers = new HashMap<>();
        try {
            patchList = patchListCache.get(change, ps);
        } catch (PatchListNotAvailableException e) {
            log.error("Patchlist is not available for {}", change.getKey(), e);
            return;
        }

        if (commit.getParentCount() != 1) {
            log.error("No merge/initial");
            return;
        }

        for (PatchListEntry entry : patchList.getPatches()) {
            log.info("Entries");
            /**
             * Only git blame at the moment. If other methods are used in the future,
             * other change types might be required.
             */
            if (entry.getChangeType() == ChangeType.MODIFIED ||
                    entry.getChangeType() == ChangeType.DELETED) {
                //TODO: Magic
                log.info("Found modified/deleted file:");
                log.info(entry.getNewName());
            }
        }
    }
}

