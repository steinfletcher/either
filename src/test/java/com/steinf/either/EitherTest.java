package com.steinf.either;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Test;

public class EitherTest {

  @Test
  public void createsARight() {
    Either<Boolean, Integer> either = Either.right(2);

    assertThat(either.right()).isEqualTo(2);
    assertThat(either.isRight()).isTrue();
    assertThat(either.isLeft()).isFalse();
    assertLeftSideAbsent(either);
  }

  @Test
  public void createsALeft() {
    Either<Boolean, Integer> either = Either.left(TRUE);

    assertThat(either.left()).isEqualTo(TRUE);
    assertThat(either.isRight()).isFalse();
    assertThat(either.isLeft()).isTrue();
    assertRightSideAbsent(either);
  }

  @Test
  public void createsARightFromSupplier() throws Exception {
    Either<Integer, String> either = Either.either(() -> 2, () -> "hi");

    assertThat(either.right()).isEqualTo("hi");
  }

  @Test
  public void createsALeftFromSupplier() throws Exception {
    Either<Integer, String> either = Either.either(() -> 2, () -> null);

    assertThat(either.left()).isEqualTo(2);
  }

  @Test
  public void foldsRight() {
    Either<Boolean, String> either = Either.right("right");

    String result = either.fold(f -> "left", f -> "newRight");

    assertThat(result).isEqualTo("newRight");
  }

  @Test
  public void foldsLeft() {
    Either<String, Boolean> either = Either.left("left");

    String result = either.fold(f -> "newLeft", f -> "right");

    assertThat(result).isEqualTo("newLeft");
  }

  @Test
  public void mapsRight() throws Exception {
    Either<Boolean, Integer> either = Either.right(20);

    Either<Boolean, Integer> mapped = either.map(value -> 10);

    assertThat(mapped.right()).isEqualTo(10);
  }

  @Test
  public void mapDoesNothingIfLeft() throws Exception {
    Either<String, Integer> either = Either.left("left");

    Either<String, Integer> mapped = either.map(value -> 10);

    assertThat(mapped.left()).isEqualTo("left");
  }

  @Test
  public void acceptsRight() throws Exception {
    Either<Boolean, Integer> either = Either.right(20);

    either.accept(left -> fail(), right -> assertThat(right).isEqualTo(20));
  }

  @Test
  public void acceptsLeft() throws Exception {
    Either<Boolean, Integer> either = Either.left(TRUE);

    either.accept(left -> assertThat(left).isEqualTo(TRUE), right -> fail());
  }

  @Test
  public void acceptsRightForSingleConsumer() throws Exception {
    Either<Boolean, Integer> either = Either.right(20);

    either.accept(right -> assertThat(right).isEqualTo(20));
  }

  @Test
  public void acceptsOnLeftIsNoOpForSingleConsumer() throws Exception {
    Either<Boolean, Integer> either = Either.left(FALSE);

    either.accept(right -> fail());
  }

  @Test
  public void shouldFlatMapRights() throws Exception {
    Either<Boolean, String> either = Either.right("right");

    Either<Boolean, String> mapped = either.flatMap(rightValue -> Either.right(rightValue + " side"));

    assertThat(mapped.right()).isEqualTo("right side");
  }

  @Test
  public void shouldPreserveLeftWhenFlatMappingOverRight() throws Exception {
    Either<Integer, String> either = Either.left(100);

    Either<Integer, String> mapped = either.andThen(rightValue -> Either.right("right"));

    assertThat(mapped.left()).isEqualTo(100);
  }

  @Test
  public void convertsToOptionalEmpty() throws Exception {
    assertThat(Either.left(100).toOptional()).isNotPresent();
  }

  @Test
  public void convertsToOptional() throws Exception {
    assertThat(Either.right(100).toOptional()).isPresent().hasValue(100);
  }

  @Test
  public void createsALeftFromAnOptional() throws Exception {
    Optional<Object> empty = Optional.empty();
    assertThat(Either.fromOptional(empty).isLeft()).isTrue();
  }

  @Test
  public void createsALeftWithSuppliedValueFromAnOptional() throws Exception {
    Optional<Object> empty = Optional.empty();
    assertThat(Either.fromOptionalOrElse(empty, () -> "left").left()).isEqualTo("left");
  }

  @Test
  public void createsARightFromAnOptional() throws Exception {
    Optional<String> present = Optional.of("present");
    assertThat(Either.fromOptional(present).right()).isEqualTo("present");
  }

  @Test
  public void returnsOtherIfRightAbsentUsingOrElse() throws Exception {
    Either<Boolean, String> left = Either.left(FALSE);

    String other = left.orElse("new right");

    assertThat(other).isEqualTo("new right");
  }

  @Test
  public void returnsOtherIfRightAbsentUsingOrElseGet() throws Exception {
    Either<Boolean, String> left = Either.left(FALSE);

    String other = left.orElseGet(() -> "new right");

    assertThat(other).isEqualTo("new right");
  }

  @Test
  public void returnsSelfIfRightUsingOrElse() throws Exception {
    Either<Boolean, String> success = Either.right("right");

    String other = success.orElse("new right");

    assertThat(other).isEqualTo("right");
  }

  @Test
  public void returnsSelfIfRightUsingOrElseGet() throws Exception {
    Either<Boolean, String> success = Either.right("right");

    String other = success.orElseGet(() -> "new right");

    assertThat(other).isEqualTo("right");
  }

  @Test
  public void extractsValueOnThrowIfRightPresent() throws Exception {
    Either<String, String> either = Either.right("right");

    String value = either.orElseThrow(() -> new RuntimeException("Err"));

    assertThat(value).isEqualTo("right");
  }

  @Test
  public void filtersOutLeftSides() {
    Stream<Either<String, String>> eithers = Stream.of(Either.left("1"), Either.right("2"), Either.right("3"));

    List<String> values = eithers
        .flatMap(Either.values())
        .collect(toList());

    assertThat(values).containsOnly("2", "3");
  }

  @Test
  public void throwsIfLeftPresent() throws Exception {
    Either<String, String> either = Either.left("right");
    assertThatThrownBy(() -> either.orElseThrow(() -> new RuntimeException("Errr")));
  }

  private <L, R> void assertRightSideAbsent(Either<L, R> either) {
    assertThatThrownBy(either::right).isInstanceOf(NoSuchElementException.class);
  }

  private <L, R> void assertLeftSideAbsent(Either<L, R> either) {
    assertThatThrownBy(either::left).isInstanceOf(NoSuchElementException.class);
  }
}
