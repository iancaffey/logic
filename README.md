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
```java
import static io.logic.CarPredicate.whenMake;
import static io.logic.CarPredicate.whenModel;
import static io.logic.StringPredicate.equalsIgnoreCase;
import static io.logic.StringPredicate.notEqualTo;

Stream<Car> cars = Stream.of(
        ImmutableCar.of("Ford", "F-150"),
        ImmutableCar.of("Ford", "Fiesta"),
        ImmutableCar.of("Honda", "Civic"),
        ImmutableCar.of("Mercedes", "C43")
);
Set<Car> fords = cars.filter(whenMake(equalsIgnoreCase("Ford"))).collect(Collectors.toSet());
[Car{make=Ford, model=Fiesta}, Car{make=Ford, model=F-150}]
```

## How to serialize your Logic

##### Creating a `Gson` that can serialize and deserialize logic predicates
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
Predicate<Car> deserialized = gson.fromJson(serialized, CarPredicate.class); //predicate.equals(deserialized) == true!!
```