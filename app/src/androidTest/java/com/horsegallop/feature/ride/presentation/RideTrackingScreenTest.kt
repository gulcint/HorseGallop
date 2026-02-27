package com.horsegallop.feature.ride.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.ride.model.GeoPoint
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import com.horsegallop.R

@RunWith(AndroidJUnit4::class)
class RideTrackingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun inactiveState_showsRideTypeSectionAndStartButton() {
        composeRule.setContent {
            RideTrackingContent(
                state = sampleIdleState(),
                hasLocationPermission = true,
                onRequestLocationPermission = {},
                onToggleRide = {},
                onSetAutoDetect = {},
                onBarnSelected = {},
                onRideTypeSelected = {},
                onDismissSavedSummary = {},
                onRetryPendingSync = {}
            )
        }

        composeRule.onNodeWithTag(RideTestTags.RideTypeSection).assertIsDisplayed()
        composeRule.onNodeWithTag(RideTestTags.StartButton).assertIsDisplayed()
        composeRule.onNodeWithTag(RideTestTags.BarnField).assertIsDisplayed()
    }

    @Test
    fun withoutPermission_showsPermissionCard() {
        composeRule.setContent {
            RideTrackingContent(
                state = sampleIdleState(),
                hasLocationPermission = false,
                onRequestLocationPermission = {},
                onToggleRide = {},
                onSetAutoDetect = {},
                onBarnSelected = {},
                onRideTypeSelected = {},
                onDismissSavedSummary = {},
                onRetryPendingSync = {}
            )
        }

        composeRule.onNodeWithTag(RideTestTags.PermissionCard).assertIsDisplayed()
    }

    @Test
    fun withoutPermission_showsSingleGrantAccessAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val grantAccessText = context.getString(R.string.ride_grant_location)

        composeRule.setContent {
            RideTrackingContent(
                state = sampleIdleState(),
                hasLocationPermission = false,
                onRequestLocationPermission = {},
                onToggleRide = {},
                onSetAutoDetect = {},
                onBarnSelected = {},
                onRideTypeSelected = {},
                onDismissSavedSummary = {},
                onRetryPendingSync = {}
            )
        }

        val grantButtons = composeRule
            .onAllNodesWithText(grantAccessText)
            .fetchSemanticsNodes()
        assertTrue(grantButtons.size == 1)
    }

    @Test
    fun ridingState_showsMapAndFinishButton() {
        composeRule.setContent {
            RideTrackingContent(
                state = sampleRidingState(),
                hasLocationPermission = true,
                onRequestLocationPermission = {},
                onToggleRide = {},
                onSetAutoDetect = {},
                onBarnSelected = {},
                onRideTypeSelected = {},
                onDismissSavedSummary = {},
                onRetryPendingSync = {}
            )
        }

        composeRule.onNodeWithTag(RideTestTags.MapCard).assertIsDisplayed()
        composeRule.onNodeWithTag(RideTestTags.FinishButton).assertIsDisplayed()
    }

    @Test
    fun pendingSync_showsRetryAction() {
        var retryTriggered = false
        composeRule.setContent {
            RideTrackingContent(
                state = sampleIdleState().copy(pendingSyncCount = 2),
                hasLocationPermission = true,
                onRequestLocationPermission = {},
                onToggleRide = {},
                onSetAutoDetect = {},
                onBarnSelected = {},
                onRideTypeSelected = {},
                onDismissSavedSummary = {},
                onRetryPendingSync = { retryTriggered = true }
            )
        }

        composeRule.onNodeWithTag(RideTestTags.SyncStatusCard).assertIsDisplayed()
        composeRule.onNodeWithTag(RideTestTags.RetrySyncButton).performClick()
        composeRule.runOnIdle {
            assertTrue(retryTriggered)
        }
    }

    @Test
    fun syncedStatus_showsStatusCardWithoutRetryButton() {
        composeRule.setContent {
            RideTrackingContent(
                state = sampleIdleState().copy(
                    pendingSyncCount = 0,
                    lastStopSyncStatus = com.horsegallop.domain.ride.model.RideSyncStatus.Synced
                ),
                hasLocationPermission = true,
                onRequestLocationPermission = {},
                onToggleRide = {},
                onSetAutoDetect = {},
                onBarnSelected = {},
                onRideTypeSelected = {},
                onDismissSavedSummary = {},
                onRetryPendingSync = {}
            )
        }

        composeRule.onNodeWithTag(RideTestTags.SyncStatusCard).assertIsDisplayed()
        val retryButtons = composeRule
            .onAllNodesWithTag(RideTestTags.RetrySyncButton)
            .fetchSemanticsNodes()
        assertTrue(retryButtons.isEmpty())
    }

    private fun sampleIdleState(): RideUiState {
        return RideUiState(
            barns = listOf(
                BarnWithLocation(
                    barn = BarnUi(
                        id = "barn-1",
                        name = "Caddebostan Arena",
                        description = "Training"
                    ),
                    lat = 41.0,
                    lng = 29.0,
                    amenities = emptySet()
                )
            ),
            selectedBarn = BarnWithLocation(
                barn = BarnUi(
                    id = "barn-1",
                    name = "Caddebostan Arena",
                    description = "Training"
                ),
                lat = 41.0,
                lng = 29.0,
                amenities = emptySet()
            ),
            savedRideSummary = SavedRideSummary(
                durationSec = 1200,
                distanceKm = 3.4f,
                calories = 180,
                avgSpeedKmh = 10.2f,
                maxSpeedKmh = 15.4f,
                rideType = RideType.TRAIL_RIDING,
                barnName = "Caddebostan Arena",
                savedAtMillis = System.currentTimeMillis()
            )
        )
    }

    private fun sampleRidingState(): RideUiState {
        return RideUiState(
            speedKmh = 12f,
            avgSpeedKmh = 10.2f,
            maxSpeedKmh = 15.4f,
            distanceKm = 3.4f,
            durationSec = 1200,
            calories = 180,
            isRiding = true,
            autoDetect = true,
            pathPoints = listOf(
                GeoPoint(41.0, 29.0),
                GeoPoint(41.01, 29.01),
                GeoPoint(41.02, 29.03)
            )
        )
    }
}
