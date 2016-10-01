import com.hypertino.binders.core.{ImplicitDeserializer, ImplicitSerializer}
import com.hypertino.binders.json.{JsonBinders, JsonDeserializer, JsonSerializer}
import org.scalatest.{FlatSpec, Matchers}
import org.threeten.bp._

class InstantTypeSerializer extends ImplicitSerializer[Instant, JsonSerializer[_]] {
  override def write(serializer: JsonSerializer[_], value: Instant): Unit = serializer.writeLong(value.toEpochMilli)
}

class InstantTypeDeserializer extends ImplicitDeserializer[Instant, JsonDeserializer[_]] {
  override def read(deserializer: JsonDeserializer[_]): Instant = Instant.ofEpochMilli(deserializer.readLong())
}

object InstantJsonBinders {
  implicit val serializer = new InstantTypeSerializer
  implicit val deserializer = new InstantTypeDeserializer
}

case class ClassWithInstant(instant: Instant)

class TestSerializerTypeExtension extends FlatSpec with Matchers {

  import JsonBinders._
  import InstantJsonBinders._

  it should " serialize extra data type" in {
    val instant = Instant.parse("2016-10-01T00:12:42.007Z")
    val str = instant.toJson
    str shouldBe "1475280762007"
  }

  it should " deserialize extra data type" in {
    val o = "1475280762007".parseJson[Instant]
    val instant = Instant.parse("2016-10-01T00:12:42.007Z")
    o shouldBe instant
  }

  it should " serialize extra data type inside class" in {
    val t = ClassWithInstant(Instant.parse("2016-10-01T00:12:42.007Z"))
    val str = t.toJson
    str shouldBe "{\"instant\":1475280762007}"
  }

  it should " deserialize extra data type to class" in {
    val o = "{\"instant\":1475280762007}".parseJson[ClassWithInstant]
    val t = ClassWithInstant(Instant.parse("2016-10-01T00:12:42.007Z"))
    o shouldBe t
  }
}
