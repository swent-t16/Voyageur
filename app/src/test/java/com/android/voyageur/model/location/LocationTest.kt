import com.android.voyageur.model.location.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationTest {

  @Test
  fun testLocationCreationWithAllFields() {
    val location =
        Location(country = "France", city = "Paris", county = "Île-de-France", zip = "75001")

    assertEquals("France", location.country)
    assertEquals("Paris", location.city)
    assertEquals("Île-de-France", location.county)
    assertEquals("75001", location.zip)
  }

  @Test
  fun testLocationCreationWithOnlyRequiredFields() {
    val location = Location(country = "United States", city = "New York")

    assertEquals("United States", location.country)
    assertEquals("New York", location.city)
    assertEquals("", location.county)
    assertEquals("", location.zip)
  }
}
