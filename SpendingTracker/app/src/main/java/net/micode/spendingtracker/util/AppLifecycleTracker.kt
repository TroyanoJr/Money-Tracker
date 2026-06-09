package net.micode.spendingtracker.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Tracks the application's foreground/background state by monitoring activity lifecycle events.
 * Extracted from SpendingTrackerApp to comply with SRP.
 */
class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    /**
     * Observable property indicating if the app is currently in the foreground.
     */
    var isAppInForeground: Boolean = false
        private set

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            isAppInForeground = true
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            isAppInForeground = false
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
