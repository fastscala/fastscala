import io.circe.Encoder

@main def hello(): Unit = {
  import io.circe.*
  import io.circe.generic.semiauto.*
  import io.circe.syntax.*

  case class Person(name: String)

  implicit val personEncoder: Encoder[Person] = deriveEncoder[Person]

  Person("Chris").asJson
}
