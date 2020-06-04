package ca.johnmajor.disjunctions;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An immutable right-biased inclusive disjunction that is either a left (an {@code L}), a right (an
 * {@code R}), or both.
 *
 * @param <L> the type of the left value
 * @param <R> the type of the right value
 * @author John Major
 */
public abstract class Ior<L, R> {
    private Ior() {}

    /**
     * Returns the result of calling one of the given functions depending on whether this is a left,
     * right or both.
     *
     * @param ifLeft  the function to call if this is a left
     * @param ifRight the function to call if this is a right
     * @param ifBoth  the function to call if this is both
     * @param <T>     the return type
     * @return the result of calling one of the given functions
     */
    public abstract <T> T fold(
            Function<? super L, ? extends T> ifLeft,
            Function<? super R, ? extends T> ifRight,
            BiFunction<? super L, ? super R, ? extends T> ifBoth
    );

    /**
     * Constructs an {@link Ior} with a right value.
     *
     * @param value the non-null right value
     * @param <L>   the type of the non-existent left value
     * @param <R>   the type of the right value
     * @return the new {@link Ior}
     * @throws NullPointerException if the right value is null
     */
    public static <L, R> Ior<L, R> right(R value) {
        return new Right<>(value);
    }

    /**
     * Constructs an {@link Ior} with a left value.
     *
     * @param value the non-null left value
     * @param <L>   the type of the left value
     * @param <R>   the type of the non-existent right value
     * @return the new {@link Ior}
     * @throws NullPointerException if the left value is null
     */
    public static <L, R> Ior<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Constructs an {@link Ior} with both a left and right value.
     *
     * @param leftValue  the non-null left value
     * @param rightValue the non-null right value
     * @param <L>        the type of the left value
     * @param <R>        the type of the right value
     * @return the new {@link Ior}
     * @throws NullPointerException if either the left or right value are null
     */
    public static <L, R> Ior<L, R> both(L leftValue, R rightValue) {
        return new Both<>(leftValue, rightValue);
    }

    /**
     * Returns a new {@link Ior} where the new right value is the result of calling the given
     * function with the current right value. If this {@link Ior} is a left the function simply
     * returns {@code this}.
     *
     * @param f   the function to call if this is a right or a both
     * @param <T> the type of the new right value
     * @return the new {@link Ior}
     */
    @SuppressWarnings("unchecked cast")
    public <T> Ior<L, T> map(Function<? super R, ? extends T> f) {
        return fold(
                l -> (Ior<L, T>) this,
                f.andThen(Right::new),
                (l, r) -> new Both<>(l, f.apply(r))
        );
    }

    /**
     * Returns a new {@link Ior} where the new left value is the result of calling the given
     * function with the current left value. If this {@link Ior} is a right the function simply
     * returns {@code this}.
     *
     * @param f   the function to call if this is a left or a both
     * @param <T> the type of the new left value
     * @return the new {@link Ior}
     */
    @SuppressWarnings("unchecked cast")
    public <T> Ior<T, R> mapLeft(Function<? super L, ? extends T> f) {
        return fold(
                f.andThen(Left::new),
                r -> (Ior<T, R>) this,
                (l, r) -> new Both<>(f.apply(l), r)
        );
    }

    /**
     * Returns a new {@link Ior} by calling the given functions on the right and left values.
     *
     * @param fa  the function to call with the left value
     * @param fb  the function to call with the right value
     * @param <C> the type of the new left value
     * @param <D> the type of the new right value
     * @return the new {@link Ior}
     */
    public <C, D> Ior<C, D> bimap(
            Function<? super L, ? extends C> fa,
            Function<? super R, ? extends D> fb
    ) {
        return fold(
                fa.andThen(Left::new),
                fb.andThen(Right::new),
                (l, r) -> new Both<>(fa.apply(l), fb.apply(r))
        );
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
     * Returns {@code true} if this is a both, {@code false} otherwise.
     */
    public boolean isBoth() {
        return this instanceof Both;
    }

    /**
     * Returns a new {@link Ior} by swapping the right and the left.
     */
    public Ior<R, L> swap() {
        return fold(Right::new, Left::new, (l, r) -> new Both<>(r, l));
    }

    /**
     * Returns an {@link Optional} containing the left value or an empty {@link Optional} if a left
     * value doesn't exist.
     */
    public Optional<L> getLeft() {
        return fold(Optional::of, r -> Optional.empty(), (l, r) -> Optional.of(l));
    }

    /**
     * Returns an {@link Optional} containing the right value or an empty {@link Optional} if a
     * right value doesn't exist.
     */
    public Optional<R> getRight() {
        return fold(l -> Optional.empty(), Optional::of, (l, r) -> Optional.of(r));
    }

    /**
     * Returns an {@link Optional} containing the left value if this is a left or an empty
     * {@link Optional} if this is a right or a both.
     */
    public Optional<L> getOnlyLeft() {
        return fold(Optional::of, r -> Optional.empty(), (l, r) -> Optional.empty());
    }

    /**
     * Returns an {@link Optional} containing the right value if this is a right or an empty
     * {@link Optional} if this is a left or a both.
     */
    public Optional<R> getOnlyRight() {
        return fold(l -> Optional.empty(), Optional::of, (l, r) -> Optional.empty());
    }

    /**
     * Returns the right value or the result of calling the given function with the left value.
     *
     * @param f the function to call if this is a left
     */
    public R valueOr(Function<? super L, ? extends R> f) {
        return fold(f, Function.identity(), (l, r) -> r);
    }

    /**
     * Returns {@code true} if the right value satisfies the given predicate, {@code false}
     * otherwise.
     */
    public boolean exists(Predicate<R> predicate) {
        return fold(l -> false, predicate::test, (l, r) -> predicate.test(r));
    }

    /**
     * Returns {@code true} if this is a left or the right value satisfies the given predicate,
     * {@code false} otherwise.
     */
    public boolean forall(Predicate<R> predicate) {
        return fold(l -> true, predicate::test, (l, r) -> predicate.test(r));
    }

    /**
     * Returns the right value or the result of calling the given function.
     *
     * @param f the function to call if this is a left
     */
    public R getOrElse(Supplier<? extends R> f) {
        return fold(l -> f.get(), Function.identity(), (l, r) -> r);
    }

    /**
     * Returns the result of calling the given function if this is a left, otherwise returns
     * {@code this}.
     *
     * @param f the function to call if this is a left
     */
    public Ior<L, R> orElse(Supplier<Ior<L, R>> f) {
        return fold(l -> f.get(), r -> this, (l, r) -> this);
    }

    /**
     * Returns {@code this} if this is a left, otherwise returns the given {@code Ior}.
     *
     * @param res the {@code Ior} to return if this is a right
     */
    @SuppressWarnings("unchecked cast")
    public <T> Ior<L, T> and(Ior<L, T> res) {
        return fold(l -> (Ior<L, T>) this, r -> res, (l, r) -> res);
    }

    /**
     * Returns the given {@link Ior} if this is a left, otherwise {@code this}.
     */
    public Ior<L, R> or(Ior<L, R> res) {
        return fold(l -> res, r -> this, (l, r) -> this);
    }

    /**
     * Encapsulates a left value.
     */
    private final static class Left<L, R> extends Ior<L, R> {
        private final L value;

        Left(L value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public <T> T fold(
                Function<? super L, ? extends T> ifLeft,
                Function<? super R, ? extends T> ifRight,
                BiFunction<? super L, ? super R, ? extends T> ifBoth
        ) {
            return ifLeft.apply(value);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj == this) ||
                    (obj instanceof Left && Objects.equals(this.value, ((Left<?, ?>) obj).value));
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
    private final static class Right<L, R> extends Ior<L, R> {
        private final R value;

        Right(R value) {
            this.value = Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public <T> T fold(
                Function<? super L, ? extends T> ifLeft,
                Function<? super R, ? extends T> ifRight,
                BiFunction<? super L, ? super R, ? extends T> ifBoth
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

    /**
     * Encapsulates both a left and a right value.
     */
    private final static class Both<L, R> extends Ior<L, R> {
        private final L leftValue;
        private final R rightValue;

        Both(L leftValue, R rightValue) {
            this.leftValue = Objects.requireNonNull(leftValue, "left value must not be null");
            this.rightValue = Objects.requireNonNull(rightValue, "right value must not be null");
        }

        @Override
        public <T> T fold(
                Function<? super L, ? extends T> ifLeft,
                Function<? super R, ? extends T> ifRight,
                BiFunction<? super L, ? super R, ? extends T> ifBoth
        ) {
            return ifBoth.apply(leftValue, rightValue);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Both<?, ?> other = (Both<?, ?>) obj;
            return leftValue.equals(other.leftValue) && rightValue.equals(other.rightValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(leftValue, rightValue);
        }

        @Override
        public String toString() {
            return String.format("Both(%s, %s)", leftValue, rightValue);
        }
    }
}
