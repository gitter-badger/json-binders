[![Build Status](https://travis-ci.org/hypertino/json-binders.svg)](https://travis-ci.org/hypertino/json-binders)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hypertino/json-binders_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hypertino/json-binders_2.11)

[ Latest releases and snapshots](https://oss.sonatype.org/#nexus-search;gav~com.hypertino~json-binders_*~~~)

# json-binders

`json-binders` is a library for Scala/Scala.js that allows you to serialize/deserialize Scala case classes, primitive types to/from JSON representation.

## Why json-binders?
There already exists numerous libraries for the same purpose, like `scala/pickling`, `upickle`, `spray-json` targeting Scala and `FasterXML/jackson`, `gson` and many others Java libraries that you may use in Scala.

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

import JsonBinders._

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
`Duration` and `FiniteDuration` are supported out of box. `Duration` is serialized as string value and `FiniteDuration` as numeric value in milliseconds. 
    
### Case-classes and normal classes/traits with companion object

As shown in example a case classes are supported out of box. Any regular class or a trait that have a companion object with corresponding `apply`/`unaply` methods 
are supported as well.

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

If you need a special name on some field, you may use an attribute `fieldName`:
Example:

```scala
case class Kid(
  @fieldName("kid name") name: String, 
  age: Int
)

Kid("John", 13).toJson // produces: {"kid name":"John","age":13}
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

Map[String, Something]

## null handling

If a field is defined as `Option[Something]` then null value is deserialized as None and vice versa.
 
If a field can't be null, like `Int` or any other primitive value, an exception will be thrown while reading `null` value.   

## Either

`json-binders` tries to match best suited value when reading `Either[_,_]`

```scala
"1".parseJson[Either[Int,String]] // returns Left(1)
```

More complex scenarios are possible with collections and objects.

## Support custom types

You may support any other type like `java.util.Date` implementing `ImplicitSerializer` and `ImplicitDeserializer`. 
Please find an example in [TestCustomDataSerializer.scala](jsonBinders/shared/src/test/scala/TestCustomDataSerializer.scala) 

## Schemaless/custom fields

TBD

# Benchmark

TBD

# Things to cover:

- naming convention converters;
- stream API;
- iterator/stream/seq shortcomings;

# License

`json-binders` is licensed under BSD 3-clause as stated in file LICENSE
