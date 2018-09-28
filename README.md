# logic
Annotation processor for producing logic definitions of Java classes for declarative programming.

## Overview
In order to enable logic programming without the crippling overhead of doing it in an OOP language, [logic](https://github.com/iancaffey/logic) does all of the nasty work for you of creating all of the object representations of your predicates.

Internally, [logic](https://github.com/iancaffey/logic) models every predicate implementation from `Logic` annotations found in source files. Then the remainder of the work is handed off to [Immutables](https://immutables.github.io/) to generate value classes for the predicates.

The major benefit of leveraging [Immutables](https://immutables.github.io/) is the value semantics and JSON serialization for all predicate implementations.

With [logic](https://github.com/iancaffey/logic), your predicates **really** are values.

Use them in hash tables, serialize them and transport them over the wire, or simply use them as Java8 `Predicate`.

## How to define your Logic
##### Option 1: User-defined Interface
```java
@Logic
@Value.Immutable //you can even mix in @Logic with Immutables directly!
public interface Car {
    @Value.Parameter
    String getMake();

    @Value.Parameter
    String getModel();
}
```
##### Option 2: User-defined Annotation
```java
@Logic(methods = "*")
public @interface Car {
    String make();

    String model();
}
```
## How to use your Logic
##### Use it to filter a stream!
```java
import static io.logic.CarPredicate.whenMake;
import static io.logic.CarPredicate.whenModel;
import static io.logic.StringPredicate.equalsIgnoreCase;

Stream<Car> cars = Stream.of(
        ImmutableCar.of("Ford", "F-150"),
        ImmutableCar.of("Ford", "Fiesta"),
        ImmutableCar.of("Honda", "Civic"),
        ImmutableCar.of("Mercedes", "C43")
);
Set<Car> fords = cars.filter(whenMake(equalsIgnoreCase("Ford"))).collect(Collectors.toSet());
//[Car{make=Ford, model=Fiesta}, Car{make=Ford, model=F-150}]
```

##### Use it as a key in a HashMap!
```java
import static io.logic.CarPredicate.whenMake;
import static io.logic.CarPredicate.whenModel;
import static io.logic.StringPredicate.equalsIgnoreCase;
import static io.logic.StringPredicate.isNotEmpty;

CarPredicate predicate = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
Map<CarPredicate, String> map = ImmutableMap.of(predicate, "FordWithNonEmptyModel");
String value = map.get(predicate);
//FordWithNonEmptyModel
```

##### Compare instances using Object#equals!

```java
import static io.logic.CarPredicate.whenMake;
import static io.logic.CarPredicate.whenModel;
import static io.logic.StringPredicate.equalsIgnoreCase;
import static io.logic.StringPredicate.isNotEmpty;

CarPredicate one = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
CarPredicate two = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
boolean equals = one.equals(two);
//true!!
```

## How to serialize your Logic

##### Creating a `Gson` that can serialize and deserialize logic predicates
Both [logic](https://github.com/iancaffey/logic) and [Immutables](https://immutables.github.io/) create a lot of `TypeAdapterFactory` for you so it's best to automatically load them from `ServiceLoader`.

One thing to note is a limitation in annotation processors is not being able to mess with each others files. This includes the META-INF files we create to register your `TypeAdapterFactory` implementations.
To keep from stepping on each others feet, [logic](https://github.com/iancaffey/logic) registers all `TypeAdapterFactory` under a shim interface `TypeAdapterFactoryMirror`. You can get the actual factory through `TypeAdapterFactoryMirror#getFactory()`.

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import io.logic.gson.TypeAdapterFactoryMirror;

GsonBuilder builder = new GsonBuilder();
ServiceLoader.load(TypeAdapterFactory.class).forEach(factory -> builder.registerTypeAdapterFactory(factory));
ServiceLoader.load(TypeAdapterFactoryMirror.class).forEach(mirror -> builder.registerTypeAdapterFactory(mirror.getFactory()));
Gson gson = builder.create();
```

#### Serializing a logic predicate
```java
Predicate<Car> predicate = whenMake(equalsIgnoreCase("Ford")).and(whenModel(notEqualTo("Fiesta")));
String serialized = gson.toJson(predicate, CarPredicate.class);
```

#### Deserializing a logic predicate
```java
Predicate<Car> predicate = whenMake(equalsIgnoreCase("Ford")).and(whenModel(notEqualTo("Fiesta")));
String serialized = gson.toJson(predicate, CarPredicate.class);
Predicate<Car> deserialized = gson.fromJson(serialized, CarPredicate.class);
```