package com.steinf.either;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An algebraic data type representing the outcome of an operation that might fail
 *
 * @param <L> the type of the left object
 * @param <R> the type of the right object
 */
public interface Either<L, R> extends Serializable {

  /**
   * Creates a left instance
   *
   * @param value the failure value
   * @param <L> the type of the success object
   * @return a service outcome containing a failure object
   * @throws NullPointerException if {@code value} is null
   */
  static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  /**
   * Creates a right instance
   *
   * @param value the success value
   * @param <R> the type of the right object
   * @return a service outcome containing a success object
   * @throws NullPointerException if {@code value} is null
   */
  static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  /**
   * Folds the failure or success to a response of type R
   *
   * @param onLeft the function to apply to the left object if present
   * @param onRight the function to apply to the right object if present
   * @return the response generated from the applied function
   */
  default <X> X fold(Function<? super L, X> onLeft, Function<? super R, X> onRight) {
    if (isRight()) {
      return onRight.apply(right());
    } else {
      return onLeft.apply(left());
    }
  }

  /**
   * Maps over the success value if present
   *
   * @param mapper the function to apply to the success
   * @param <T> the success type after the function application
   * @return a new {@link Either} containing the existing failure and a new transformed success value. If success is
   * empty this method only has the effect of translating the object's return type.
   */
  @SuppressWarnings("unchecked")
  default <T> Either<L, T> map(Function<? super R, ? extends T> mapper) {
    if (isRight()) {
      return new Right<>(mapper.apply(right()));
    } else {
      return (Either<L, T>) this;
    }
  }

  /**
   * Maps over the success value if present. Like map but flattens out nested Try chains
   *
   * @param mapper the function to apply to success
   * @param <U> The success type after function application
   * @return a new {@link Either} containing the existing failure and a new transformed success value. If success is
   * empty this method only has the effect of translating the object's return type.
   */
  @SuppressWarnings("unchecked")
  default <U> Either<L, U> flatMap(final Function<? super R, Either<L, U>> mapper) {
    if (isRight()) {
      return mapper.apply(right());
    } else {
      return (Either<L, U>) this;
    }
  }

  /**
   * Alias for {@link this#flatMap(Function)}
   */
  default <U> Either<L, U> andThen(final Function<? super R, Either<L, U>> mapper) {
    return flatMap(mapper);
  }

  /**
   * Applies the given consumers to both the success and failure if their values are present
   *
   * @param leftConsumer the consumer of the failure, only applied if failure is not empty
   * @param rightConsumer the consumer of the success, only applued if the success is not empty
   */
  default void accept(Consumer<L> leftConsumer, Consumer<R> rightConsumer) {
    if (isRight()) {
      rightConsumer.accept(right());
    } else {
      leftConsumer.accept(left());
    }
  }

  /**
   * Applies the given consumers to both the right value if present, otherwise peforms no operation
   *
   * @param rightConsumer the consumer of the right value, only applied if the right value is present
   */
  default void accept(Consumer<R> rightConsumer) {
    if (isRight()) {
      rightConsumer.accept(right());
    }
  }

  /**
   * Return the success value if present, otherwise return {@code other}.
   *
   * @param other the value to be returned if there is no success value present
   * @return the value, if present, otherwise {@code other}
   * @throws NullPointerException if other is null
   */
  default Either<L, R> orElse(Supplier<Either<L, R>> other) {
    if (isRight()) {
      return this;
    } else {
      return other.get();
    }
  }

  /**
   * Return the success value if present, otherwise throw {@code exceptionSupplier}.
   *
   * @param exceptionSupplier the exception to throw if the right value is not present
   * @return the right value, if present, otherwise {@code other}
   */
  default <X extends Throwable> R orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (isRight()) {
      return right();
    } else {
      throw exceptionSupplier.get();
    }
  }

  /**
   * Creates a java optional from the right side. Loses the left side.
   *
   * @return an optional wrapping the right value
   */
  Optional<R> toOptional();

  /**
   * Creates a right sided either from a java optional if present. Loses the left side if the optional is present.
   *
   * @return an optional wrapping the right value
   */
  static <L, R> Either<L, R> fromOptional(Optional<R> optional) {
     return optional.<Either<L, R>>map(Either::right).orElseGet(() -> Either.left(null));
  }

  /**
   * Creates an either from a java optional. Takes the left side from the supplier
   * if the optional is empty.
   *
   * @return an optional wrapping the right value
   */
  static <L, R> Either<L, R> fromOptionalOrElse(Optional<R> optional, Supplier<L> left) {
    return optional.<Either<L, R>>map(Either::right).orElseGet(() -> Either.left(left.get()));
  }

  /**
   * Gets the value of success
   *
   * @return the value stored in success
   * @throws UnsupportedOperationException if no such value is present
   */
  R right();

  /**
   * Gets the value of the failure
   *
   * @return the value stored in the failure
   * @throws UnsupportedOperationException if no such value is present
   */
  L left();

  /**
   * @return true on success, false on failure
   */
  boolean isRight();

  /**
   * @return true on success, false on failure
   */
  boolean isLeft();

  final class Right<L, R> implements Either<L, R> {

    private final R value;

    private Right(R value) {
      this.value = value;
    }

    @Override
    public Optional<R> toOptional() {
      return Optional.of(value);
    }

    @Override
    public R right() {
      return value;
    }

    @Override
    public L left() {
      throw new NoSuchElementException("No left value present");
    }

    @Override
    public boolean isRight() {
      return true;
    }

    @Override
    public boolean isLeft() {
      return false;
    }
  }

  final class Left<L, R> implements Either<L, R> {

    private final L value;

    private Left(L value) {
      this.value = value;
    }

    @Override
    public Optional<R> toOptional() {
      return Optional.empty();
    }

    @Override
    public R right() {
      throw new NoSuchElementException("No right value present");
    }

    @Override
    public L left() {
      return value;
    }

    @Override
    public boolean isRight() {
      return false;
    }

    @Override
    public boolean isLeft() {
      return true;
    }
  }
}

