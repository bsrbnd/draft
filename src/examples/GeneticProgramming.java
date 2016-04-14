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
import java.util.Arrays;

import static symprog.MethodSymbol.ExpressionSymbol;

/**
 * Example of a symbolic regression using the symbolic annotation processor SymProc.
 *
 * For information about genetic programming concepts, see:
 * http://www.genetic-programming.org/
 *
 * 
 * @author Bernard Blaser
 *
 */
class GeneticProgramming {
	// Population characteristics

	private static final int MAX_DEPTH = 3;
	private static final int REPRODUCTION = 2, CROSSOVER = 78, MUTATION = 20;
	private static final int POP_SIZE = REPRODUCTION + CROSSOVER + MUTATION;

	// Integral computation

	private static final double DX = 0.1;
	@Symbolic private static final double LOWER_BOUND = -1.;
	@Symbolic private static final double UPPER_BOUND = 1.;

	// Execution

	private static final int MAX_ITER = 300;
	private static final int STEP = 5; // Display granularity
	private static final double END = 0.01;

	// Terminal symbols

	// Constants (two possibilities: final fields (1) or constant methods (2))
	// (1) Final Fields
	// @Symbolic private final Double a = 1., b = 2.;
	// (2) Constant Methods
	@Symbolic private Double a() {return 1.;}
	@Symbolic private Double b() {return 2.;}
	// Variable
	@Symbolic private Double x;

	// Functions

	@Symbolic private Double add(Double i, Double j) {return i + j;}
	@Symbolic private Double sub(Double i, Double j) {return i - j;}
	@Symbolic private Double mul(Double i, Double j) {return i * j;}
	@Symbolic private Double abs(Double i) {return i < 0. ? -i : i;}

	// Target function: x*x - 2*x + 1 (two possibilities: with final fields (1) or with constant methods (2))

	// (1) Final Fields
	// private final MethodSymbol $target = $add.apply($sub.apply($mul.apply($x,$x), $mul.apply($b, $x)), $a);
	// (2) Constant Methods: notice that a constant method symbol can be used like $b or $a.apply()
	private final MethodSymbol $target = $add.apply($sub.apply($mul.apply($x,$x), $mul.apply($b, $x)), $a.apply());

	// Functions and terminals sets used to construct expressions

	private MethodSymbol[] functions = new MethodSymbol[] {$add, $sub, $mul};
	private Symbol<?>[] terminals = new Symbol<?>[] {$a, $b, $x};

	// Evaluation of the individuals

	private Double fitness(Symbol<?> $f) throws Exception {
		// Two variants: semi-imperative computation (1) or full functional with quotation to prevent evaluation (2)
		// (1) Semi-imperative
		// return integral(LOWER_BOUND, UPPER_BOUND, $abs.apply($sub.apply($f,$target)));
		// (2) Full functional with quotation to prevent evaluation
		Symbol<?> $g = $integral.apply($LOWER_BOUND, $UPPER_BOUND, $abs.apply($sub.apply($f,$target)).quote());
		return (Double)$g.evaluate(this);
	}

	@Symbolic private Double integral(Double a, Double b, Symbol<?> $f) throws Exception {
		Double dx = b-a; // abs() not necessary since b > a
		if (dx <= DX) {
			x = (a+b)/2.;
			// Full functional with value capture
			return (Double)$mul.apply($f,new Value(dx)).evaluate(this);
		}
		else {
			Double m = (a+b)/2.;
			return integral(a,m,$f) + integral(m,b,$f);
		}
	}

	// Random generation of individuals

	private int randomIndex(int size) {
		return (int) (Math.random() * size);
	}

	private Symbol<?> randomExpression(int maxDepth) {
		if (maxDepth==0) {
			return terminals[randomIndex(terminals.length)];
		}
		else {
			if (randomIndex(10) > 3) { // 70%
				MethodSymbol $func = functions[randomIndex(functions.length)];
				Symbol<?> $term1 = randomExpression(maxDepth-1);
				Symbol<?> $term2 = randomExpression(maxDepth-1);
				return $func.apply($term1, $term2);
			}
			else {
				return terminals[randomIndex(terminals.length)];
			}
		}
	}

	// Genetic operations (mutation and crossover)

	private boolean mutation(Term $s, int maxDepth) {
		boolean modif = false;
		if ($s instanceof ExpressionSymbol) {
			ExpressionSymbol $f = (ExpressionSymbol)$s;
			if (randomIndex(10) > 3) { // 70%
				$f.TERMS[randomIndex($f.TERMS.length)] = randomExpression(maxDepth-1);
				modif = true;
			}
			else {
				for (Term $e: $f.TERMS) {
					if (mutation($e, maxDepth-1)) {
						modif = true;
						break;
					}
				}
			}
		}
		return modif;
	}

	private boolean crossover(Term $a, Term $b) { // maxDepth not necessary
		boolean modif = false;
		if (($a instanceof ExpressionSymbol) && ($b instanceof ExpressionSymbol)) {
			ExpressionSymbol $f = (ExpressionSymbol)$a, $g = (ExpressionSymbol)$b;
			if (randomIndex(10) > 3) { // 70%
				int i = randomIndex($f.TERMS.length);
				Term $t = $f.TERMS[i];
				$f.TERMS[i] = $g.TERMS[i];
				$g.TERMS[i] = $t;
				modif = true;
			}
			else {
				for (Term $ef: $f.TERMS) {
					for (Term $eg: $g.TERMS) {
						modif = crossover($ef, $eg);
						if (modif) {break;}
					}
					if (modif) {break;}
				}
			}
		}
		return modif;
	}

	// Individual representation

	private static class Individual implements Comparable<Individual> {
		@Symbolic // Only for use in toString()
		public Double fitness;

		public final Symbol<?> $expression;

		public Individual(Symbol<?> $expression, Double fitness) {
			this.$expression = $expression; this.fitness = fitness;
		}
		@Override
		public int compareTo(Individual d) {
			return fitness.compareTo(d.fitness);
		}

		@Override
		public String toString() {
			return $expression + " " + $fitness + "=" + fitness;
		}
	}

	public void run() {
		try {
			// Final fields:
			// String given = "|-given: " + $a + "=" + a + "," + $b + "=" + b;
			// Or constant methods:
			String given = "|-given: " + $a + "=" + a() + "," + $b + "=" + b();

			System.out.println("Target: " + $target);
			System.out.println(given + "," + $integral + "=" + integral(LOWER_BOUND, UPPER_BOUND, $target));

			Individual[] pop = new Individual[POP_SIZE];

			for (int i=0; i<POP_SIZE; i++) {
				Symbol<?> $expr = randomExpression(MAX_DEPTH);
				pop[i] = new Individual($expr, fitness($expr));
			}
			for (int j=0; j<MAX_ITER; j++) {
				// Sort by fitness
				Arrays.sort(pop);

				if (j % STEP == 0) {
					System.out.println("Generation " + j);
					for (int k=0; k<5; k++) {
						System.out.println("|-individual " + k + ": " + pop[k]);
					}

					// TODO Could be checked at every iteration
					if (pop[0].fitness < END) {break;}
				}

				// TODO Symbol.clone() for genetic operations (but not necessary since no need of new individuals)

				// The best ones are reproduced without modification.
				// Crossover
				for (int k=REPRODUCTION; k<REPRODUCTION+CROSSOVER; k+=2) {
					crossover(pop[k].$expression, pop[k+1].$expression);
					pop[k].fitness = fitness(pop[k].$expression);
					pop[k+1].fitness = fitness(pop[k+1].$expression);
				}
				// Mutations
				for (int k=REPRODUCTION+CROSSOVER; k<POP_SIZE; k++) {
					mutation(pop[k].$expression, MAX_DEPTH);
					pop[k].fitness = fitness(pop[k].$expression);
				}
			}
			System.out.println("Best: " + pop[0]);
		}
		catch (Exception e) {e.printStackTrace();}
	}

	public static void main(String[] args) {
		new GeneticProgramming().run();
	}
}
