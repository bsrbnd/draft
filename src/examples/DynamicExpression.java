/*
 * Copyright 2016 Bernard Blaser
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

import symprog.*;

class DynamicExpression {
	@Symbolic private Integer a = 3;
	private static interface B {@Symbolic Integer b();}
	private static interface C {@Symbolic Integer c();}

	@Symbolic public Integer add(Integer i, Integer j) {return i + j;}
	@Symbolic public Integer sub(Integer i, Integer j) {return i - j;}

	public static void main(String[] args) {
		try {
			B b = new B() {public Integer b() {return 2;}}; // Anonymous class
			C c = () -> {return 1;}; // Lambda

			Symbol<?> $b = B.$b.bind(b);
			Symbol<?> $c = C.$c.bind(c);

			Symbol<?> $symbolicExpression = $add.apply($a, $sub.apply($b, $c));
			Integer d = (Integer)$symbolicExpression.evaluate(new DynamicExpression());

			System.out.println($symbolicExpression + " evaluates to " + d);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
