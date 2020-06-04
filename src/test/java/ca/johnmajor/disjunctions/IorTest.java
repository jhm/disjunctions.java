package ca.johnmajor.disjunctions;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class IorTest {
    @Test
    void rightThrowsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> Ior.right(null));
    }

    @Test
    void leftThrowsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> Ior.left(null));
    }

    @Test
    void bothThrowsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> Ior.both(null, 1));
        assertThatNullPointerException().isThrownBy(() -> Ior.both(1, null));
    }

    @Test
    void foldOnRight() {
        Ior<String, Integer> right = Ior.right(1);
        assertThat(right.fold(l -> 0, Function.identity(), (a, b) -> 0)).isEqualTo(1);
    }

    @Test
    void foldOnLeft() {
        Ior<String, Integer> left = Ior.left("left");
        assertThat(left.fold(String::length, r -> 0, (a, b) -> 0)).isEqualTo(4);
    }

    @Test
    void foldOnBoth() {
        Ior<String, String> both = Ior.both("left", "right");
        int result = both.fold(String::length, String::length, (a, b) -> a.length() + b.length());
        assertThat(result).isEqualTo(9);
    }

    @Test
    void mapOnRight() {
        Ior<String, Integer> right = Ior.right(2);
        assertThat(right.map(n -> n * n)).isEqualTo(Ior.right(4));
    }

    @Test
    void mapOnLeft() {
        Ior<String, Integer> left = Ior.left("left");
        assertThat(left.map(n -> n * n)).isSameAs(left);
    }

    @Test
    void mapOnBoth() {
        Ior<Integer, Integer> both = Ior.both(1, 5);
        assertThat(both.map(n -> n * n)).isEqualTo(Ior.both(1, 25));
    }

    @Test
    void mapLeftOnRight() {
        Ior<String, Integer> right = Ior.right(5);
        assertThat(right.mapLeft(String::toUpperCase)).isSameAs(right);
    }

    @Test
    void mapLeftOnLeft() {
        Ior<String, Integer> left = Ior.left("left");
        assertThat(left.mapLeft(String::length)).isEqualTo(Ior.left(4));
    }

    @Test
    void mapLeftOnBoth() {
        Ior<Integer, Integer> both = Ior.both(5, 1);
        assertThat(both.mapLeft(n -> n * n)).isEqualTo(Ior.both(25, 1));
    }

    @Test
    void bimapOnRight() {
        Ior<Integer, Integer> right = Ior.right(5);
        assertThat(right.bimap(l -> 0, r -> r * r)).isEqualTo(Ior.right(25));
    }

    @Test
    void bimapOnLeft() {
        Ior<Integer, Integer> left = Ior.left(5);
        assertThat(left.bimap(l -> l * l, r -> 0)).isEqualTo(Ior.left(25));
    }

    @Test
    void bimapOnBoth() {
        Ior<Integer, Integer> both = Ior.both(2, 5);
        assertThat(both.bimap(l -> 0, r -> 1)).isEqualTo(Ior.both(0, 1));
    }

    @Test
    void isLeftOnLeftReturnsTrue() {
        assertThat(Ior.left(0).isLeft()).isTrue();
    }

    @Test
    void isLeftOnRightReturnsFalse() {
        assertThat(Ior.right(0).isLeft()).isFalse();
    }

    @Test
    void isLeftOnBothReturnsFalse() {
        assertThat(Ior.both(0, 1).isLeft()).isFalse();
    }

    @Test
    void isRightOnLeftReturnsFalse() {
        assertThat(Ior.left(0).isRight()).isFalse();
    }

    @Test
    void isRightOnRightReturnsTrue() {
        assertThat(Ior.right(0).isRight()).isTrue();
    }

    @Test
    void isRightOnBothReturnsFalse() {
        assertThat(Ior.both(0, 1).isRight()).isFalse();
    }

    @Test
    void isBothOnLeftReturnsFalse() {
        assertThat(Ior.left(0).isBoth()).isFalse();
    }

    @Test
    void isBothOnRightReturnsFalse() {
        assertThat(Ior.right(0).isBoth()).isFalse();
    }

    @Test
    void isBothOnBothReturnsTrue() {
        assertThat(Ior.both(0, 1).isBoth()).isTrue();
    }

    @Test
    void getLeftOnLeft() {
        Ior<Integer, Integer> left = Ior.left(1);
        assertThat(left.getLeft()).contains(1);
    }

    @Test
    void getLeftOnRight() {
        Ior<Integer, Integer> right = Ior.right(5);
        assertThat(right.getLeft()).isEmpty();
    }

    @Test
    void getLeftOnBoth() {
        Ior<Integer, Integer> both = Ior.both(1, 5);
        assertThat(both.getLeft()).contains(1);
    }

    @Test
    void getRightOnLeft() {
        Ior<Integer, Integer> left = Ior.left(1);
        assertThat(left.getRight()).isEmpty();
    }

    @Test
    void getRightOnRight() {
        Ior<Integer, Integer> right = Ior.right(5);
        assertThat(right.getRight()).contains(5);
    }

    @Test
    void getRightOnBoth() {
        Ior<Integer, Integer> both = Ior.both(1, 5);
        assertThat(both.getRight()).contains(5);
    }

    @Test
    void getOnlyLeftOnLeft() {
        Ior<Integer, Integer> left = Ior.left(1);
        assertThat(left.getOnlyLeft()).contains(1);
    }

    @Test
    void getOnlyLeftOnRight() {
        Ior<Integer, Integer> right = Ior.right(5);
        assertThat(right.getOnlyLeft()).isEmpty();
    }

    @Test
    void getOnlyLeftOnBoth() {
        Ior<Integer, Integer> both = Ior.both(1, 5);
        assertThat(both.getOnlyLeft()).isEmpty();
    }

    @Test
    void getOnlyRightOnLeft() {
        Ior<Integer, Integer> left = Ior.left(1);
        assertThat(left.getOnlyRight()).isEmpty();
    }

    @Test
    void getOnlyRightOnRight() {
        Ior<Integer, Integer> right = Ior.right(5);
        assertThat(right.getOnlyRight()).contains(5);
    }

    @Test
    void getOnlyRightOnBoth() {
        Ior<Integer, Integer> both = Ior.both(1, 5);
        assertThat(both.getOnlyRight()).isEmpty();
    }

    @Test
    void existsOnLeft() {
        Ior<Integer, Integer> left = Ior.left(2);
        assertThat(left.exists(n -> n % 2 == 0)).isFalse();
    }

    @Test
    void existsOnRight() {
        Ior<Integer, Integer> right = Ior.right(2);
        assertThat(right.exists(n -> n % 2 == 0)).isTrue();
        assertThat(right.exists(n -> n % 2 != 0)).isFalse();
    }

    @Test
    void existsOnBoth() {
        Ior<Integer, Integer> both = Ior.both(1, 2);
        assertThat(both.exists(n -> n % 2 == 0)).isTrue();
        assertThat(both.exists(n -> n % 2 != 0)).isFalse();
    }

    @Test
    void forallOnLeft() {
        Ior<Integer, Integer> left = Ior.left(2);
        assertThat(left.forall(n -> n % 2 != 0)).isTrue();
    }

    @Test
    void forallOnRight() {
        Ior<Integer, Integer> right = Ior.right(2);
        assertThat(right.forall(n -> n % 2 == 0)).isTrue();
        assertThat(right.forall(n -> n % 2 != 0)).isFalse();
    }

    @Test
    void forallOnBoth() {
        Ior<Integer, Integer> both = Ior.both(1, 2);
        assertThat(both.forall(n -> n % 2 == 0)).isTrue();
        assertThat(both.forall(n -> n % 2 != 0)).isFalse();
    }

    @Test
    void swap() {
        Ior<Integer, Integer> left = Ior.<Integer, Integer>right(0).swap();
        assertThat(left.isLeft()).isTrue();
        assertThat(left).isEqualTo(Ior.left(0));
    }

    @Test
    void valueOrReturnsTheRightValue() {
        Ior<String, Integer> right = Ior.right(2);
        assertThat(right.valueOr(n -> 0)).isEqualTo(2);

        Ior<Integer, Integer> both = Ior.both(1, 2);
        assertThat(both.valueOr(n -> 0)).isEqualTo(2);
    }

    @Test
    void valueOrReturnsTheFunctionResult() {
        Ior<String, Integer> left = Ior.left("left");
        assertThat(left.valueOr(n -> 0)).isEqualTo(0);
    }

    @Test
    void orElse() {
        Ior<String, Integer> right = Ior.right(2);
        assertThat(right.orElse(() -> Ior.left("left"))).isEqualTo(Ior.right(2));
    }

    @Test
    void orElseReturnsDefault() {
        Ior<String, Integer> left = Ior.left("left");
        assertThat(left.orElse(() -> Ior.right(2))).isEqualTo(Ior.right(2));
    }

    @Test
    void or() {
        Ior<String, Integer> right = Ior.right(0);
        assertThat(right.or(Ior.right(1))).isSameAs(right);
    }

    @Test
    void orReturnsDefault() {
        Ior<String, Integer> left = Ior.left("left");
        Ior<String, Integer> result = Ior.right(0);
        assertThat(left.or(result)).isSameAs(result);
    }

    @Test
    void andOnRight() {
        Ior<String, Integer> right = Ior.right(0);
        assertThat(right.and(Ior.right(2))).isEqualTo(Ior.right(2));
    }

    @Test
    void andOnLeft() {
        Ior<String, Integer> left = Ior.left("left");
        assertThat(left.and(Ior.right(2))).isSameAs(left);
    }

    @Test
    void andOnBoth() {
        Ior<Integer, Integer> both = Ior.both(0, 1);
        assertThat(both.and(Ior.right(2))).isEqualTo(Ior.right(2));
    }
}
