package com.android.voyageur.notifications

import com.android.voyageur.model.notifications.TripInvite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TripInviteTest {

  @Test
  fun testTripInviteCreationWithAllFields() {
    val tripInvite = TripInvite(id = "1", tripId = "2", from = "3", to = "4")

    assertEquals("1", tripInvite.id)
    assertEquals("2", tripInvite.tripId)
    assertEquals("3", tripInvite.from)
    assertEquals("4", tripInvite.to)
  }

  @Test
  fun testTripInviteEquals() {
    val tripInvite1 = TripInvite(id = "1", tripId = "2", from = "3", to = "4")
    val tripInvite2 = TripInvite(id = "1", tripId = "2", from = "3", to = "4")
    assertEquals(tripInvite1, tripInvite2)
  }

  @Test
  fun testTripInviteHashCode() {
    val tripInvite1 = TripInvite(id = "1", tripId = "2", from = "3", to = "4")
    val tripInvite2 = TripInvite(id = "1", tripId = "2", from = "3", to = "4")
    assertEquals(tripInvite1.hashCode(), tripInvite2.hashCode())
  }

  @Test
  fun testTripInviteNotEquals() {
    val tripInvite1 = TripInvite(id = "1", tripId = "2", from = "3", to = "4")
    val tripInvite2 = TripInvite(id = "1", tripId = "2", from = "3", to = "5")
    assertNotEquals(tripInvite1, tripInvite2)
  }

  @Test
  fun testTripInviteNotEqualsDifferentType() {
    val tripInvite = TripInvite(id = "1", tripId = "2", from = "3", to = "4")
    assertNotEquals(tripInvite, "tripInvite")
  }
}
