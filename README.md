[![Build Status](https://travis-ci.org/hypertino/json-binders.svg)](https://travis-ci.org/hypertino/json-binders)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hypertino/json-binders_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hypertino/json-binders_2.11)

[ Latest releases and snapshots](https://oss.sonatype.org/#nexus-search;gav~com.hypertino~json-binders_*~~~)

# json-binders

`json-binders` is a library for Scala/Scala.js that allows you to serialize/deserialize Scala case classes, primitive types to/from JSON representation.

## Why json-binders?

There already exists a numerous libraries for the same purpose, like `scala/pickling`, `upickle`, `spray-json` targeting Scala and `FasterXML/jackson`, `gson` and many others Java libraries that you may use in Scala.

Key features of `json-binders` are:

1. Compile-time code generation without runtime reflection
2. Streaming/iterative underlying API
3. Clean/interoperable JSON format
4. Schemaless fields support
5. Scala.js support

The most close alternative is a [uPickle](http://www.lihaoyi.com/upickle-pprint/upickle/), and the differences that `json-binders` gives you are:

1. 2-3 times performance boost, see below on performance benchmark
2. ability to work with schemaless fields (`Value` type)
3. `FasterXML/jackson` dependency for JVM compilation instead of `Jaws` that is used by `uPickle`

# Download

Add to project with SBT: `"com.hypertino" %% "json-binders" % "1.0-SNAPSHOT"`

# Quickstart

A plain example on how-to start using a library:

```scala
case class Crocodile(
  name: String,
  length: Int,
  color: Option[String]
)

import com.hypertino.binders.json.JsonBinders._

val crocodileJson = Crocodile("Gena", 250, Some("Green")).toJson

// crocodileJson: String = {"name":"Gena","length":250,"color":"Green"}

val crocodile = crocodileJson.parseJson[Crocodile]
```

That's it. If you work with string representation of JSON then you only have to use `toJson`/`parseJson` macro calls. 

# Supported types

### Primitive types

`json-binders` supports primitive types: `Int`, `Long`, `Double`, `Float`, `BigDecimal` and `Boolean` with `String`.
> Please note that `Long` range is limited due to fact that Javascript's Number type (64 bit IEEE 754) only has about 53 bits of precision


### `Duration` and `FiniteDuration`
`Duration` and `FiniteDuration` are supported out of box. `Duration` is serialized as a string value and `FiniteDuration` as a numeric value in milliseconds. 
    
### Case-classes and normal classes/traits with companion object

As it shown in an example the case-classes are supported out of box. Any regular class or a trait that have a companion object with corresponding `apply`/`unaply` methods are supported as well.

#### Default case class values

Case-classes can have default values specified on fields. And `json-binders` will return default field value in case if JSON source doesn't contains that field or the value of the field is `null`.  
Example:

```scala
case class Zoo(
  name: String,
  open: Boolean = true
)

val zoo = """{"name":"Moscow Zoo"}""".parseJson[Zoo]
// zoo.open is true here
```

#### Case-class field names

If you need a special name on some field, you may set it with an attribute `fieldName`:
Example:

```scala
case class Kid(
  @fieldName("name of kid") name: String, 
  age: Int
)

Kid("John", 13).toJson // produces: {"name of kid":"John","age":13}
```

## Collections

You can read and write almost any Scala collection class.
```
List(1,2,3).toJson // produces `[1,2,3]` 

"[1,2,3]".parseJson[List[Int]] // produces List[Int] = List(1, 2, 3) 

// more complex case:
List(Kid("John", 13), Kid("Anthony", 12), Kid("Ellie", 13)).toJson
```
All collection items have to be bindable (primitive or a case-class or collection, etc). 
In general any collection that implements `canBuildFrom` is supported.

### `Map[String, Something]`

`Map[String, Something]` is a special case and it is serialized as a JSON object. The `Something` here can be any bindable type.

## null handling

If a field is defined as `Option[Something]` then null value is deserialized as None and vice versa.
 
If a field can't be null, like `Int` or any other primitive value, an exception will be thrown while reading `null` value.   

## Either

`json-binders` tries to match best suited value when reading `Either[_,_]`

```scala
"1".parseJson[Either[Int,String]] // returns Left(1)
```

More complex scenarios are possible with collections and objects.

## Custom types

You may support any custom type implementing `ImplicitSerializer` and `ImplicitDeserializer` traits.

Example:

```scala
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

import JsonBinders._
import InstantJsonBinders._

val instantJson = Instant.parse("2016-10-01T00:12:42.007Z").toJson
// instantJson: String = 1475280762007

val instant = instantJson.parseJson[Instant]
// instant: org.threeten.bp.Instant = 2016-10-01T00:12:42.007Z
```

## Schemaless/custom fields

`binders` library provides a `Value` type that:

- Implements the [`scala.Dynamic`](http://www.scala-lang.org/api/current/index.html#scala.Dynamic) to access object fields using dynamic invocation
- Can be a `Null`, `Obj`, `Lst`, `Text`, `Number` or `Bool`
- Guarantees that it's serializable to JSON
- Provides a `visitor pattern` interface to `Value` tree

One of the examples of using `Value` is when you need some custom, schema-free data inside some class. Example:

```scala
import com.hypertino.binders.value._

case class Crocodile(
  name: String,
  color: Option[String],
  extra: Value          // <- custom extra data stored here
)

import com.hypertino.binders.json.JsonBinders._

val crocodileJson = Crocodile("Gena", Some("Green"), 
  ObjV("country" -> "Russia", "age" -> 49) // this constructs Obj type for extra field
).toJson

// crocodileJson: String = {"name":"Gena","color":"Green","extra":{"country":"Russia","age":49}}

val crocodile = crocodileJson.parseJson[Crocodile]

val country = crocodile.extra.country // accessing field through `scala.Dynamic` 
println(country.toString)

val age = crocodile.extra.age // returned type is `Value` instance type `Number`
println(age.toInt)
```

# Benchmark

Benchmark test is done using `jmh` for JVM and with a simple and naive implementation for JS. Benchmark is run with sbt:

- for JVM run `benchTestJVM/jmh:run`
- for JS run `benchTestJS/run`

Here is the results that I get on Macbook Pro Core i5 2.5GHz:

TBD


# Things to cover

- naming convention converters;
- stream API;
- iterator/stream/seq shortcomings;

# License

`json-binders` is licensed under BSD 3-clause as stated in file LICENSE
