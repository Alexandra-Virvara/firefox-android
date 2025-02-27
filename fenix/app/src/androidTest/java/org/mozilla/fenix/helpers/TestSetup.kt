/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.fenix.helpers

import android.util.Log
import kotlinx.coroutines.runBlocking
import mozilla.components.browser.state.store.BrowserStore
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.helpers.Constants.TAG
import org.mozilla.fenix.ui.robots.notificationShade
import java.util.Locale

/**
 * Standard Test setup and tear down methods to run before each test.
 * Some extra clean-up is required when we're using the org.mozilla.fenix.helpers.RetryTestRule (the instrumentation does not do that in this case).
 *
 */
open class TestSetup {
    lateinit var mockWebServer: MockWebServer
    lateinit var browserStore: BrowserStore

    @Before
    open fun setUp() {
        Log.i(TAG, "TestSetup: Starting the @Before setup")
        runBlocking {
            // Reset locale to EN-US if needed.
            // Because of https://bugzilla.mozilla.org/show_bug.cgi?id=1812183, some items might not be updated.
            if (Locale.getDefault() != Locale.US) {
                AppAndSystemHelper.setSystemLocale(Locale.US)
            }
            // Check and clear the downloads folder, in case the tearDown method is not executed.
            // This will only work in case of a RetryTestRule execution.
            AppAndSystemHelper.clearDownloadsFolder()
            // Make sure the Wifi and Mobile Data connections are on.
            AppAndSystemHelper.setNetworkEnabled(true)
            // Clear bookmarks left after a failed test, before a retry.
            AppAndSystemHelper.deleteBookmarksStorage()
            // Clear history left after a failed test, before a retry.
            AppAndSystemHelper.deleteHistoryStorage()
            // Clear permissions left after a failed test, before a retry.
            AppAndSystemHelper.deletePermissionsStorage()
        }

        // Initializing this as part of class construction, below the rule would throw a NPE.
        // So we are initializing this here instead of in all related tests.
        Log.i(TAG, "TestSetup: Trying to initialize the browserStore instance")
        browserStore = TestHelper.appContext.components.core.store
        Log.i(TAG, "TestSetup: Initialized the browserStore instance")
        // Clear pre-existing notifications.
        notificationShade {
            cancelAllShownNotifications()
        }

        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
        }
        try {
            Log.i(TAG, "Try starting mockWebServer")
            mockWebServer.start()
        } catch (e: Exception) {
            Log.i(TAG, "Exception caught. Re-starting mockWebServer")
            mockWebServer.shutdown()
            mockWebServer.start()
        }
    }

    @After
    open fun tearDown() {
        Log.i(TAG, "TestSetup: Starting the @After tearDown methods.")
        runBlocking {
            // Check and clear the downloads folder.
            AppAndSystemHelper.clearDownloadsFolder()

            // Reset locale to EN-US if needed.
            // This method is only here temporarily, to set the language before a new activity is started.
            // TODO: When https://bugzilla.mozilla.org/show_bug.cgi?id=1812183 is fixed, it should be removed.
            if (Locale.getDefault() != Locale.US) {
                AppAndSystemHelper.setSystemLocale(Locale.US)
            }
        }
    }
}
