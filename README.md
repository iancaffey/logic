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
public interface Car {
    String getMake();

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
##### Option 3: Including files you don't even own?!
```java
@Logic.Include(value = java.awt.Point.class, logic = @Logic(fields = {})) //we can even ignore the public fields that Point exposes for some unknown reason. :-)
package cool.project;
```
You use it just as if you defined the logic within your codebase!
```java
import static io.logic.DoublePredicate.equalTo;
import static io.logic.DoublePredicate.lessThan;
import static cool.project.PointPredicate.whenX;
import static cool.project.PointPredicate.whenY;

Predicate<Point> predicate = whenX(lessThan(47)).and(whenY(equalTo(15)));
```
##### Option 4: Intermingling with an Immutables abstract value type!
```java
@Logic
@Value.Immutable
public interface Car {
    @Value.Parameter
    String getMake();

    @Value.Parameter
    String getModel();
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

Predicate<Car> key = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
Map<Predicate<Car>, String> map = ImmutableMap.of(key, "FordWithNonEmptyModel");

Predicate<Car> duplicate = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
String value = map.get(duplicate);
//FordWithNonEmptyModel
```

##### Compare instances using Object#equals!

```java
import static io.logic.CarPredicate.whenMake;
import static io.logic.CarPredicate.whenModel;
import static io.logic.StringPredicate.equalsIgnoreCase;
import static io.logic.StringPredicate.isNotEmpty;

Predicate<Car> one = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
Predicate<Car> two = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
boolean equals = one.equals(two);
//true!!
```

##### Leverage the Visitor pattern!
```java
import static io.logic.CarPredicate.whenMake;
import static io.logic.CarPredicate.whenModel;
import static io.logic.StringPredicate.equalsIgnoreCase;
import static io.logic.StringPredicate.isNotEmpty;

CarPredicate predicate = whenMake(equalsIgnoreCase("Ford")).and(whenModel(isNotEmpty()));
String value = predicate.accept(new CarPredicateVisitor<String>() {
    @Override
    public String visit(CarPredicate.And and) {
        return "Wow, an And!";
    }

    @Override
    public String visit(CarPredicate.Or or) {
        return "Wow, an Or!";
    }

    @Override
    public String visit(CarPredicate.Not not) {
        return "Wow, a Not!";
    }

    @Override
    public String visit(CarPredicate.Make make) {
        return "Wow, a Make predicate!";
    }

    @Override
    public String visit(CarPredicate.Model model) {
        return "Wow, a Model predicate!";
    }
});
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
[logic](https://github.com/iancaffey/logic) predicates serialize like any other Java object you'd serialize with [gson](https://github.com/google/gson). 

The one caveat being you need to specify the predicate class directly instead of the Java8 `Predicate` class so [gson](https://github.com/google/gson) can resolve the runtime type information properly.

```java
Predicate<Car> predicate = whenMake(equalsIgnoreCase("Ford")).and(whenModel(notEqualTo("Fiesta")));
String serialized = gson.toJson(predicate, CarPredicate.class);
```

#### Deserializing a logic predicate
Like with serialization, deserialization with [logic](https://github.com/iancaffey/logic) predicates works just as you would expect with [gson](https://github.com/google/gson).

```java
Predicate<Car> predicate = whenMake(equalsIgnoreCase("Ford")).and(whenModel(notEqualTo("Fiesta")));
String serialized = gson.toJson(predicate, CarPredicate.class);
Predicate<Car> deserialized = gson.fromJson(serialized, CarPredicate.class);
```