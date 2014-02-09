import reactivemongo.api._
import reactivemongo.bson._
import scala.concurrent._
import spray.routing._
import spray.httpx._
import spray.json._

case class TestEntity(id: Option[BSONObjectID], name: String, age: Int)

object TestEntityBSONFormat {
  implicit object TestEntityBSONReader extends BSONDocumentReader[TestEntity] {
    def read(doc: BSONDocument) = TestEntity(
      id = doc.getAs[BSONObjectID]("_id"),
      name = doc.getAs[String]("name").get,
      age = doc.getAs[Int]("age").get
    )
  }

  implicit object TestEntityBSONWriter extends BSONDocumentWriter[TestEntity] {
    def write(obj: TestEntity): BSONDocument = BSONDocument(
      "_id" -> obj.id,
      "name" -> obj.name,
      "age" -> obj.age
    )
  }
}

object TestEntityJSONFormat  extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object BSONObjectIDFormat extends JsonFormat[BSONObjectID] {
    def write(id: BSONObjectID) = JsString(id.stringify)
    def read(value: JsValue) =
      value match {
        case JsString(str) => BSONObjectID(str)
        case _ => deserializationError("BSON ID expected: " + value)
      }
  }

  implicit val testEntityJSONFormat = jsonFormat3(TestEntity)
}

object Database {
  import TestEntityBSONFormat._

  implicit val executor = scala.concurrent.ExecutionContext.Implicits.global

  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
  val database = connection("reactivemongo-benchmark")
  val collection = database("collection")

  def get() = collection.find(BSONDocument.empty).one[TestEntity]

  def fill() {
    collection.drop()

    for (i <- 1 to 1000) {
      val e = TestEntity(id = Some(BSONObjectID.generate), name = "e" + i, age = i)
      Await.result(collection.insert(e), duration.Duration.Inf)
    }
  }
}
