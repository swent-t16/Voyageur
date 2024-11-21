import com.android.voyageur.model.location.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationTest {

  @Test
  fun testLocationCreationWithAllFields() {
    val location = Location(address = "Big Ben Cafe")

    assertEquals("Big Ben Cafe", location.address)
  }
}
