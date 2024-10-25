import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.user.User
import org.junit.Assert.*
import org.junit.Before

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
}
