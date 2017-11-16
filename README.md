# either

[![CircleCI](https://circleci.com/gh/steinfletcher/either/tree/master.svg?style=shield)](https://circleci.com/gh/steinfletcher/either/tree/master)
[![Lambda](https://img.shields.io/maven-central/v/com.steinf/either.svg)](http://search.maven.org/#search%7Cga%7C1%7Ccom.steinf.either)
[![codecov](https://codecov.io/gh/steinfletcher/either/branch/master/graph/badge.svg)](https://codecov.io/gh/steinfletcher/either)

A simple algebraic data type for java. An Either is commonly used to represent a result that might fail,
where the left side contains the error result and the right side contains the success result. You can think
of this as a more powerful alternative to `Optional`. Plays nicely with Java 8 types.

## Artifacts

Gradle

    compile 'com.steinf:either:1.0.4'

Maven

    <dependency>
        <groupId>com.steinf</groupId>
        <artifactId>either</artifactId>
        <version>1.0.4</version>
    </dependency>

## Examples

Create an either
```Java
Either<String, Integer> e = Either.right(2);
Either<String, Integer> e = Either.left("FAIL");
Either<String, Integer> e = Either.either(() -> "FAIL", () -> 2); // right biased
Either<String, Integer> e = Either.fromOptional(Optional.of(2));
Either<String, Integer> e = Either.fromOptionalOrElse(Optional.empty(), () -> "FAIL");
```

Extract the `right` or `left` value
```Java
Either<String, Integer> e = Either.right(20);
e.getRight() // == 20
e.getLeft() // throws NoSuchElementException

Either<String, Integer> e = Either.left(20);
e.getLeft() // == 20
e.getRight() // throws NoSuchElementException
```

Check whether the either `isLeft` or `isRight`
```Java
Either.right(20).isRight() // true
Either.right(20).isLeft() // false
```

`map` over an either
```Java
Either.right(20)
      .map(e -> e * 10); // == Either.right(200)
```

Combine either with `flatMap` or `andThen`
```Java
Either.right("right")
      .flatMap(rightValue -> Either.right(rightValue + " side")); // == EIther.right("right side")
```

`fold` an either into a value
```Java
Either.right("right")
      .fold(f -> "left", f -> f + " side"); // == "right side"

Either.left("left")
      .fold(f -> f + " side", f -> "right"); // == "left side"
```

`accept` an either
```Java
Either.right("right")
      .accept(l -> {}, r -> {});

Either.right("right")
      .accept(r -> {});
```

Extract the value `orElse` get the provided value if Left
```Java
Either.left(FALSE);
      .orElse("right");  // == "right"

Either.right(10);
      .orElse(99);  // == 10
```

Extract the value `orElseGet` get the provided value if Left
```Java
Either.left(FALSE);
      .orElseGet(() -> "right");  // == "right"

Either.right(10);
      .orElseGet(() -> 99);  // == 10
```

Extract the value `orElseThrow` if Left
```Java
Either.left(FALSE);
      .orElseThrow(() -> new RuntimeException("Errr"));  // throws RuntimeException

Either.right(100);
      .orElseThrow(() -> new RuntimeException("Errr"));  // == 100
```

Filter the right side `values` from a stream
```Java
Stream<Either<String, String>> eithers = Stream.of(
    Either.left("1"),
    Either.right("2"),
    Either.right("3"));

eithers
    .flatMap(Either.values())
    .collect(toList()); // == [2, 3]
```

Convert `toOptional`
```Java
Either.left(FALSE)
      .toOptional(); // == Optional.empty()

Either.right(10);
      .toOptional(); // == Optional.of(10)
```
