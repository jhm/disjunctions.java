package ca.johnmajor.disjunctions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EitherTest {
    @Test
    void rightThrowsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> Either.right(null));
    }

    @Test
    void leftThrowsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> Either.left(null));
    }

    @Test
    void foldOnRight() {
        Either<String, Integer> right = Either.right(1);
        assertThat(right.fold(l -> 0, identity())).isEqualTo(1);
    }

    @Test
    void foldOnLeft() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.fold(String::length, r -> 0)).isEqualTo(4);
    }

    @Test
    void mapOnRight() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.map(n -> n * n)).isEqualTo(Either.right(4));
    }

    @Test
    void mapOnLeft() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.map(n -> n * n)).isSameAs(left);
    }

    @Test
    void mapLeftOnRight() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.mapLeft(String::toUpperCase)).isSameAs(right);
    }

    @Test
    void mapLeftOnLeft() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.mapLeft(String::toUpperCase)).isEqualTo(Either.left("LEFT"));
    }

    private static Either<String, Integer> validateEven(int n) {
        return n % 2 != 0 ? Either.left("Number is odd") : Either.right(n);
    }

    @Test
    void flatMapOnRight() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.flatMap(EitherTest::validateEven)).isEqualTo(Either.right(2));
    }

    @Test
    void flatMapOnLeft() {
        Either<String, Integer> right = Either.right(3);
        assertThat(right.flatMap(EitherTest::validateEven)).isEqualTo(
                Either.left("Number is odd"));
    }

    @Test
    void isLeftOnLeftReturnsTrue() {
        assertThat(Either.left(0).isLeft()).isTrue();
    }

    @Test
    void isLeftOnRightReturnsFalse() {
        assertThat(Either.right(0).isLeft()).isFalse();
    }

    @Test
    void isRightOnLeftReturnsFalse() {
        assertThat(Either.left(0).isRight()).isFalse();
    }

    @Test
    void isRightOnRightReturnsTrue() {
        assertThat(Either.right(0).isRight()).isTrue();
    }

    @Test
    void swap() {
        Either<Integer, String> left = Either.<String, Integer>right(0).swap();
        assertThat(left.isLeft()).isTrue();
        assertThat(left).isEqualTo(Either.left(0));
    }

    @Test
    void bimapOnRight() {
        Either<String, Integer> right = Either.<String, Integer>right(2).bimap(
                identity(), n -> n * n);
        assertThat(right.isRight()).isTrue();
        assertThat(right).isEqualTo(Either.right(4));
    }

    @Test
    void bimapOnLeft() {
        Either<String, Integer> left = Either.<String, Integer>left("left").bimap(
                String::toUpperCase, identity());
        assertThat(left.isLeft()).isTrue();
        assertThat(left).isEqualTo(Either.left("LEFT"));
    }

    @Test
    void valueOrReturnsTheRightValue() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.valueOr(n -> 0)).isEqualTo(2);
    }

    @Test
    void valueOrReturnsTheFunctionResult() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.valueOr(String::length)).isEqualTo(4);
    }

    @Test
    void existsReturnsTrue() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.exists(n -> n % 2 == 0)).isTrue();
    }

    @Test
    void existsReturnsFalse() {
        Either<String, Integer> right = Either.right(3);
        assertThat(right.exists(n -> n % 2 == 0)).isFalse();
    }

    @Test
    void existsReturnsFalseForLeft() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.exists(n -> n % 2 == 0)).isFalse();
    }

    @Test
    void forallReturnsTrue() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.forall(n -> n % 2 == 0)).isTrue();
    }

    @Test
    void forallReturnsFalse() {
        Either<String, Integer> right = Either.right(3);
        assertThat(right.forall(n -> n % 2 == 0)).isFalse();
    }

    @Test
    void forallReturnsTrueForLeft() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.forall(n -> n % 2 == 0)).isTrue();
    }

    @Test
    void getOrElse() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.getOrElse(() -> 10)).isEqualTo(2);
    }

    @Test
    void getOrElseReturnsDefault() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.getOrElse(() -> 10)).isEqualTo(10);
    }

    @Test
    void orElse() {
        Either<String, Integer> right = Either.right(2);
        assertThat(right.orElse(() -> Either.left("left"))).isEqualTo(Either.right(2));
    }

    @Test
    void orElseReturnsDefault() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.orElse(() -> Either.right(2))).isEqualTo(Either.right(2));
    }

    @Test
    void andOnRight() {
        Either<String, Integer> right = Either.right(0);
        assertThat(right.and(Either.right(2))).isEqualTo(Either.right(2));
    }

    @Test
    void andOnLeft() {
        Either<String, Integer> left = Either.left("left");
        assertThat(left.and(Either.right(2))).isSameAs(left);
    }

    @Test
    void orOnRight() {
        Either<String, Integer> right = Either.right(0);
        assertThat(right.or(Either.right(1))).isSameAs(right);
    }

    @Test
    void orOnLeft() {
        Either<String, Integer> left = Either.left("fail");
        Either<String, Integer> result = Either.right(0);
        assertThat(left.or(result)).isSameAs(result);
    }

    @Test
    void toListOnRight() {
        Either<String, Integer> right = Either.right(0);
        assertThat(right.toList()).isEqualTo(List.of(0));
    }

    @Test
    void toListOnLeftReturnsEmptyList() {
        Either<String, Integer> left = Either.left("fail");
        assertThat(left.toList()).isEqualTo(List.of());
    }

    @Test
    void toOptionalOnRight() {
        Either<String, Integer> right = Either.right(0);
        assertThat(right.toOptional()).contains(0);
    }

    @Test
    void toOptionalOnLeft() {
        Either<String, Integer> left = Either.left("fail");
        assertThat(left.toOptional()).isEmpty();
    }
}
