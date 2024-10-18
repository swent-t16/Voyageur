import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.user.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserTest {

  private lateinit var user: User
  private lateinit var friend: User
  private lateinit var trip: Trip

  @Before
  fun setUp() {
    user = User(id = "user1", name = "John Doe", email = "john.doe@example.com")
    friend = User(id = "friend1", name = "Jane Doe", email = "jane.doe@example.com")
    trip = Trip(id = "trip1", name = "Paris Trip", description = "A trip to Paris")
  }

  @Test
  fun testAddTrip() {
    user.addTrip(trip)
    assertTrue(user.trips.contains(trip))
    assertEquals(1, user.trips.size)
  }

  @Test
  fun testRemoveTrip() {
    user.addTrip(trip)
    user.removeTrip(trip)
    assertFalse(user.trips.contains(trip))
    assertEquals(0, user.trips.size)
  }

  @Test
  fun testAddFriend() {
    user.addFriend(friend)
    assertTrue(user.friends.contains(friend))
    assertEquals(1, user.friends.size)
  }

  @Test
  fun testRemoveFriend() {
    user.addFriend(friend)
    user.removeFriend(friend)
    assertFalse(user.friends.contains(friend))
    assertEquals(0, user.friends.size)
  }

  @Test
  fun testTripsAreImmutable() {
    user.addTrip(trip)
    val trips = user.trips
    try {
      (trips as MutableList).add(trip)
      fail("Trips list should be immutable")
    } catch (e: UnsupportedOperationException) {
      // Success - modification attempt should throw exception
    }
  }

  @Test
  fun testFriendsAreImmutable() {
    user.addFriend(friend)
    val friends = user.friends
    try {
      (friends as MutableList).add(friend)
      fail("Friends list should be immutable")
    } catch (e: UnsupportedOperationException) {
      // Success - modification attempt should throw exception
    }
  }
}
