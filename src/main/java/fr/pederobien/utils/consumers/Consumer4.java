package fr.pederobien.utils.consumers;

import java.util.Objects;

@FunctionalInterface
public interface Consumer4<T, T1, T2, T3> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t  The first input argument.
	 * @param t1 The second input argument.
	 * @param t2 The third input argument.
	 * @param t3 The fourth input argument.
	 */
	void accept(T t, T1 t1, T2 t2, T3 t3);

	/**
	 * Returns a composed {@code Consumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the composed
	 * operation. If performing this operation throws an exception, the
	 * {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code Consumer} that performs in sequence this operation
	 *         followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	default Consumer4<T, T1, T2, T3> andThen(Consumer4<? super T, ? super T1, ? super T2, ? super T3> after) {
		Objects.requireNonNull(after);
		return (T t, T1 t1, T2 t2, T3 t3) -> {
			accept(t, t1, t2, t3);
			after.accept(t, t1, t2, t3);
		};
	}
}
