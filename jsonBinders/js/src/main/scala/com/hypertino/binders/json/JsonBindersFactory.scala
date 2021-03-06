package com.hypertino.binders.json

import com.hypertino.binders.core.{Deserializer, Serializer}
import com.hypertino.binders.json.api.{JsonBindersFactoryApi, JsonGeneratorApi, JsonParserApi}
import com.hypertino.inflector.naming.{Converter, PlainConverter}

import scala.scalajs.js
import scala.scalajs.js.JSON

trait JsonBindersFactory[C <: Converter, S <: Serializer[C], D <: Deserializer[C]]
  extends JsonBindersFactoryApi[C, S, D] {

  def withStringParser[T](jsonString: String)(codeBlock: D ⇒ T): T = {
    val adapter = new JsParserAdapter(JSON.parse(jsonString))
    val jds = createDeserializer(adapter)
    codeBlock(jds)
  }

  def withJsonObjectParser[T](jsonObject: js.Dynamic)(codeBlock: D ⇒ T): T = {
    val adapter = new JsParserAdapter(jsonObject)
    val jds = createDeserializer(adapter)
    codeBlock(jds)
  }

  def withStringGenerator(codeBlock: S ⇒ Unit): String = {
    val sb = new StringBuilder
    val generator = new JsGeneratorAdapter(sb)
    val js = createSerializer(generator)
    codeBlock(js)
    sb.toString
  }

  def createSerializer(jsonGenerator: JsonGeneratorApi): S
  def createDeserializer(jsonParser: JsonParserApi): D
  def prettyPrint: Boolean = false
}

object JsonBindersFactory {
  implicit val defaultJsonBindersFactory = new DefaultJsonBindersFactory[PlainConverter.type]
  def findFactory[C <: Converter, S <: Serializer[C], D <: Deserializer[C]]()
    (implicit factory: JsonBindersFactory[C, S, D]): JsonBindersFactory[C, S, D] = factory
}
