package ca.johnmajor.disjunctions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable right-biased exclusive disjunction that is either a left (an {@code L}) or a right
 * (an {@code R}).
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 * @author John Major
 */
public abstract class Either<L, R> {
    private Either() {}

    /**
     * Returns the result of calling the first function with the left value or the result of calling
     * the second function with the right value.
     * <p>
     * This is the catamorphism over {@link Either}.
     *
     * @param ifLeft  the function to call if this is a left
     * @param ifRight the function to call if this is a right
     * @param <T>     the return type
     * @return the result of calling one of the given functions
     */
    public abstract <T> T fold(
            Function<? super L, ? extends T> ifLeft,
            Function<? super R, ? extends T> ifRight
    );

    /**
     * Constructs an {@link Either} with a right value.
     *
     * @param value the non-null right value
     * @param <L>   the type of the non-existent left value
     * @param <R>   the type of the right value
     * @return the new {@link Either}
     * @throws NullPointerException if the right value is null
     */
    public static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * Constructs an {@link Either} with a left value.
     *
     * @param value the non-null left value
     * @param <L>   the type of the left value
     * @param <R>   the type of the non-existent left value
     * @return the new {@link Either}
     * @throws NullPointerException if the left value is null
     */
    public static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Returns a new {@link Either} where the new right value is the result of calling the given
     * function with the current right value. If this {@link Either} is a left the function simply
     * returns {@code this}.
     *
     * @param f   the function to call if this is a right
     * @param <T> the type of the new right value
     * @return the new {@link Either}
     */
    public <T> Either<L, T> map(Function<? super R, ? extends T> f) {
        return flatMap(f.andThen(Right::new));
    }

    /**
     * Returns a new {@link Either} where the new left value is the result of calling the given
     * function with the current left value. If this {@link Either} is a right the function simply
     * returns {@code this}.
     *
     * @param f   the function to call if this is a left
     * @param <T> the type of the new left value
     * @return the new {@link Either}
     */
    @SuppressWarnings("unchecked cast")
    public <T> Either<T, R> mapLeft(Function<? super L, ? extends T> f) {
        return fold(f.andThen(Left::new), r -> (Either<T, R>) this);
    }

    /**
     * Returns {@code this} if this is a left, otherwise returns the result of calling the given
     * function with the right value.
     * <p>
     * This is otherwise known as monadic bind, >>=, andThen etc.
     *
     * @param f the function to call if this is a right
     */
    @SuppressWarnings("unchecked cast")
    public <T> Either<L, T> flatMap(Function<? super R, Either<L, T>> f) {
        return fold(l -> (Either<L, T>) this, f);
    }

    /**
     * Returns {@code true} if this is a left, {@code false} otherwise.
     */
    public boolean isLeft() {
        return this instanceof Left;
    }

    /**
     * Returns {@code true} if this is a right, {@code false} otherwise.
     */
    public boolean isRight() {
        return this instanceof Right;
    }

    /**
     * Returns a new {@link Either} by swapping the right and the left.
     */
    public Either<R, L> swap() {
        return fold(Right::new, Left::new);
    }

    /**
     * Returns a new {@link Either} by calling one of the given functions depending on whether
     * this is a left or a right.
     *
     * @param ifLeft  the function to call if this is a left
     * @param ifRight the function to call if this is a right
     * @param <A>     the type of the new left value
     * @param <B>     the type of the new right value
     * @return the new {@link Either}
     */
    public <A, B> Either<A, B> bimap(
            Function<? super L, ? extends A> ifLeft,
            Function<? super R, ? extends B> ifRight
    ) {
        return fold(ifLeft.andThen(Left::new), ifRight.andThen(Right::new));
    }

    /**
     * Returns the right value or the result of calling the given function with the left value.
     *
     * @param f the function to call if this is a left
     */
    public R valueOr(Function<? super L, ? extends R> f) {
        return fold(f, Function.identity());
    }

    /**
     * Returns {@code true} if this is a right and the right value satisfies the given predicate,
     * {@code false} otherwise.
     */
    public boolean exists(Predicate<? super R> predicate) {
        return fold(l -> false, predicate::test);
    }

    /**
     * Returns {@code true} if this is a left or the right value satisfies the given predicate,
     * {@code false} otherwise.
     */
    public boolean forall(Predicate<? super R> predicate) {
        return fold(l -> true, predicate::test);
    }

    /**
     * Returns the right value or the result of calling the given function.
     *
     * @param f the function to call if this is a left
     */
    public R getOrElse(Supplier<? extends R> f) {
        return fold(l -> f.get(), Function.identity());
    }

    /**
     * Returns the result of calling the given function if this is a left, otherwise returns
     * {@code this}.
     *
     * @param f the function to call if this is a left
     */
    public Either<L, R> orElse(Supplier<Either<L, R>> f) {
        return fold(l -> f.get(), r -> this);
    }

    /**
     * Returns {@code this} if this is a left, otherwise returns the given {@code Either}.
     *
     * @param res the {@code Either} to return if this is a right
     */
    @SuppressWarnings("unchecked cast")
    public <T> Either<L, T> and(Either<L, T> res) {
        return fold(l -> (Either<L, T>) this, r -> res);
    }

    /**
     * Returns the given {@link Either} if this is a left, otherwise {@code this}.
     */
    public Either<L, R> or(Either<L, R> res) {
        return fold(l -> res, r -> this);
    }

    /**
     * Returns an empty {@code List} if this is a left, otherwise a {@code List} containing only the
     * right value.
     * <p>
     * Note that the returned list is "immutable" and will throw a runtime exception if its mutating
     * methods are called. This method may prove useful at times but using a proper immutable list
     * implementation that doesn't throw runtime exceptions should be preferred.
     */
    public List<R> toList() {
        return fold(l -> List.of(), List::of);
    }

    /**
     * Returns an {@link Optional} containing the right value or an empty {@link Optional} if this
     * is a left.
     */
    public Optional<R> toOptional() {
        return fold(l -> Optional.empty(), Optional::of);
    }

    /**
     * Encapsulates a left value.
     */
    private static final class Left<L, R> extends Either<L, R> {
        private final L value;

        Left(L value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public <T> T fold(
                Function<? super L, ? extends T> ifLeft,
                Function<? super R, ? extends T> ifRight
        ) {
            return ifLeft.apply(value);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj == this) ||
                    (obj instanceof Left && Objects.equals(value, ((Left<?, ?>) obj).value));
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return String.format("Left(%s)", value);
        }
    }

    /**
     * Encapsulates a right value.
     */
    private static final class Right<L, R> extends Either<L, R> {
        private final R value;

        Right(R value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public <T> T fold(
                Function<? super L, ? extends T> ifLeft,
                Function<? super R, ? extends T> ifRight
        ) {
            return ifRight.apply(value);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj == this) ||
                    (obj instanceof Right && Objects.equals(value, ((Right<?, ?>) obj).value));
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return String.format("Right(%s)", value);
        }
    }
}
