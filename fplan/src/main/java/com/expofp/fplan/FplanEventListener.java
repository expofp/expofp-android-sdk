package com.expofp.fplan;

/**
 * FPlan events listener
 */
public interface FplanEventListener {

    /**
     * FPlan configured event
     */
    void onFpConfigured();

    /**
     * Booth selection event
     * @param boothName Selected booth name
     */
    void onBoothSelected(String boothName);

    /**
     * Route creation event
     * @param route Route info
     */
    void onRouteCreated(Route route);
}
