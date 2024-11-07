import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.user.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserTest {

  private lateinit var user: User
  private lateinit var trip: Trip

  @Before
  fun setUp() {
    user = User(id = "user1", name = "John Doe", email = "john.doe@example.com")
    trip = Trip(id = "trip1", name = "Paris Trip", description = "A trip to Paris")
  }

  @Test
  fun testEquals_sameProperties_shouldReturnTrue() {
    val user1 =
        User(
            id = "1",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "picture.jpg",
            bio = "Bio",
            contacts = listOf("2", "3"),
            interests = listOf("Hiking", "Reading"),
            username = "testuser")

    val user2 =
        User(
            id = "1",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "picture.jpg",
            bio = "Bio",
            contacts = listOf("2", "3"),
            interests = listOf("Hiking", "Reading"),
            username = "testuser")

    assertEquals(user1, user2)
    assertEquals(user1.hashCode(), user2.hashCode())
  }

  @Test
  fun testEquals_differentProperties_shouldReturnFalse() {
    val user1 =
        User(
            id = "1",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "picture.jpg",
            bio = "Bio",
            contacts = listOf("2", "3"),
            interests = listOf("Hiking", "Reading"),
            username = "testuser")

    val user2 =
        User(
            id = "2",
            name = "Another User",
            email = "another@example.com",
            profilePicture = "another.jpg",
            bio = "Another Bio",
            contacts = listOf("3", "4"),
            interests = listOf("Swimming", "Cooking"),
            username = "anotheruser")

    assertNotEquals(user1, user2)
    assertNotEquals(user1.hashCode(), user2.hashCode())
  }

  @Test
  fun testEquals_sameInterestsDifferentOrder_shouldReturnTrue() {
    val user1 =
        User(
            id = "1",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "picture.jpg",
            bio = "Bio",
            contacts = listOf("2", "3"),
            interests = listOf("Hiking", "Reading"),
            username = "testuser")

    val user2 =
        User(
            id = "1",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "picture.jpg",
            bio = "Bio",
            contacts = listOf("3", "2"), // Different order of contacts
            interests = listOf("Reading", "Hiking"),
            username = "testuser")

    assertEquals(user1, user2)
    assertEquals(user1.hashCode(), user2.hashCode())
  }

  @Test
  fun testEquals_differentInterests_shouldReturnFalse() {
    val user1 =
        User(
            id = "1",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "picture.jpg",
            bio = "Bio",
            contacts = listOf("2", "3"),
            interests = listOf("Hiking", "Reading"),
            username = "testuser")

    val user2 =
        User(
            id = "1",
            name = "Test User",
            email = "test@example.com",
            profilePicture = "picture.jpg",
            bio = "Bio",
            contacts = listOf("2", "3"),
            interests = listOf("Swimming", "Reading"), // Different interests
            username = "testuser")

    assertNotEquals(user1, user2)
    assertNotEquals(user1.hashCode(), user2.hashCode())
  }
}
