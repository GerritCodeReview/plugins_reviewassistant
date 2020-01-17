package com.github.reviewassistant.reviewassistant;

import com.github.reviewassistant.reviewassistant.models.Calculation;
import com.google.gerrit.server.change.RevisionResource;

/** The AdviceCache interface is used to store and fetch calculations. */
public interface AdviceCache {

  /**
   * Returns the calculation object for the matching RevisionResource.
   *
   * <p>Calculation is retrieved from cache, or computed if not present.
   *
   * @param resource the RevisionResource to fetch calculation for from the cache
   * @return a Calculation object if one is found, null otherwise
   */
  Calculation fetchCalculation(RevisionResource resource);
}
