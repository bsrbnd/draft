/*
 * Copyright 2016-2017 Bernard Blaser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples;

import symprog.Symbolic;
import symprog.Term;
import java.util.function.*;

import static symprog.Symbolic.*;

class DynamicExpression {
	class Inner {
		@Symbolic(value="_", flags=SAME)
		private Integer a = 3;

		private Integer add(Integer i, Integer j) { return i + j; }
	}

	static interface B {
		@Symbolic("_")
		Integer b();
	}

	@FunctionalInterface
	static interface C extends Supplier<Integer> {
		@Symbolic("_")
		@Override
		Integer get();
	}

	public static void main(String[] args) {
		new DynamicExpression().run();
	}

	void run() {
		try {
			// Anonymous class
			Term _b = B._b.bind(
				new B() {
					public Integer b() {return 2;}
				}
			);

			// Lambda
			Term _c = C._get.bind(
				(C) () -> 1
			);

			Inner i = new Inner();

			BiFunction<Integer, Integer, Integer> add = i::add;
			add = add.andThen(z->10*z);

			Term _symExpr = DynExprAdd._apply.bind(add).build(
				i._a.bind(i),
				DynExprSub._sub.bind(DynExprSub.SUB).build(_b, _c)
			);
			Integer d = (Integer)_symExpr.evaluate();

			System.out.println(_symExpr + " evaluates to " + d);

		}
		catch (Exception e) {e.printStackTrace();}
	}
}

@FunctionalInterface
interface DynExprAdd<I, J, R> extends BiFunction<I, J, R> {
	@Symbolic(value="_", origin="java.util.function.BiFunction")
	@Override
	R apply(I i, J j);
}

enum DynExprSub {
	SUB {Integer sub(Integer i, Integer j) { return i - j; }};

	@Symbolic("_")
	abstract Integer sub(Integer i, Integer j);
}
