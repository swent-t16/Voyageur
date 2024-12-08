package com.android.voyageur.ui.trip

import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripType
import com.google.firebase.Timestamp
import org.junit.Test

class TripTest {

  @Test
  fun tripInstancesWithIdenticalFieldsShouldBeEqual() {
    val participants = listOf("user1", "user2")
    val activities = listOf(Activity("Activity 1"), Activity("Activity 2"))
    val photos = listOf("http://example.com/image.jpg")

    val trip1 =
        Trip(
            id = "1",
            participants = participants,
            description = "Trip Description",
            name = "Trip Name",
            location = Location("", "", ""),
            startDate = Timestamp(0, 0),
            endDate = Timestamp(0, 0),
            activities = activities,
            type = TripType.BUSINESS,
            imageUri = "http://example.com/image.jpg",
            photos = photos,
            discoverable = false)

    val trip2 =
        Trip(
            id = "1",
            participants = participants,
            description = "Trip Description",
            name = "Trip Name",
            location = Location("", "", ""),
            startDate = Timestamp(0, 0),
            endDate = Timestamp(0, 0),
            activities = activities,
            type = TripType.BUSINESS,
            imageUri = "http://example.com/image.jpg",
            photos = photos,
            discoverable = false)
    assert(trip1.equals(trip2))
    assert(trip1.hashCode() == trip2.hashCode())
  }

  @Test
  fun tripInstancesWithDifferentFieldsShouldNotBeEqual() {
    val trip1 = Trip(id = "1", description = "Trip Description", name = "Trip Name")

    val trip2 = Trip(id = "2", description = "Different Description", name = "Different Name")

    assert(!trip1.equals(trip2))
  }

  @Test
  fun tripTypeShouldReturnTheCorrectTripType() {
    val trip = Trip(type = TripType.BUSINESS)
    assert(trip.tripType == TripType.BUSINESS)
  }

  @Test
  fun equalsShouldHandleNullAndDifferentObjectTypes() {
    val trip = Trip(id = "1")

    assert(!trip.equals(null))
  }

  @Test
  fun hashCodeShouldBeConsistentForTheSameObject() {
    val trip = Trip(id = "1", description = "Trip Description", name = "Trip Name")

    val initialHashCode = trip.hashCode()
    assert(trip.hashCode() == initialHashCode)
  }

  @Test
  fun defaultValuesShouldBeCorrectlyAssigned() {
    val trip = Trip()

    assert(trip.id.isEmpty())
    assert(trip.participants.isEmpty())
    assert(trip.description.isEmpty())
    assert(trip.name.isEmpty())
    assert(trip.location == Location("", "", "", 0.0, 0.0))
    assert(trip.activities.isEmpty())
    assert(trip.type == TripType.TOURISM)
    assert(trip.imageUri.isEmpty())
    assert(trip.photos.isEmpty())
    assert(!trip.discoverable)
  }
}
