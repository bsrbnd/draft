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

	// Constants (notice that final fields would be also possible)
	private static final double A=1., B=2.;
	@Symbolic private Double a() {return A;}
	@Symbolic private Double b() {return B;}
	// Variable (free, but bound to an instance)
	@Symbolic private Double x;

	// Functions

	@Symbolic private static Double add(Double i, Double j) {return i + j;}
	@Symbolic private static Double sub(Double i, Double j) {return i - j;}
	@Symbolic private static Double mul(Double i, Double j) {return i * j;}
	@Symbolic private static Double abs(Double i) {return i < 0. ? -i : i;}

	// Target formula (not a function) with the free variable x: x*x - 2*x + 1
	// (notice that a constant method symbol can be used like $b or $a.apply())
	private final Term $target = $add.apply($sub.apply($mul.apply($x,$x), $mul.apply($b, $x)), $a.apply()).bind(this);

	// Functions and terminals sets used to construct expressions

	private MethodSymbol[] functions = new MethodSymbol[] {$add, $sub, $mul};
	private Term[] terminals = new Term[] {new Value(A), $b.bind(this), $x.bind(this)}; // Notice the captured value A.

	// Evaluation of the individuals

	private Double fitness(Term $f) {
		// Using a lambda to transform a formula to a function.
		Fx g = x -> {this.x=x; return (Double)$abs.apply($sub.apply($f,$target)).evaluate();};
		return integral(LOWER_BOUND, UPPER_BOUND, Fx.$f.bind(g));
	}

	public static interface Fx {@Symbolic Double f(Double x);}

	@Symbolic private static Double integral(Double a, Double b, MethodSymbol $f) {
		Double dx = b-a, m = (a+b)/2.; // abs(b-a) not necessary since b > a
		if (dx <= DX) {
			// Fully symbolic with value capture
			return (Double)$mul.apply($f.apply(new Value(m)),new Value(dx)).evaluate();
		}
		else {
			return integral(a,m,$f) + integral(m,b,$f);
		}
	}

	// Random generation of individuals

	private int randomIndex(int size) {
		return (int) (Math.random() * size);
	}

	private Term randomExpression(int maxDepth) {
		if (maxDepth==0) {
			return terminals[randomIndex(terminals.length)];
		}
		else {
			if (randomIndex(10) > 3) { // 70%
				MethodSymbol $func = functions[randomIndex(functions.length)];
				Term $term1 = randomExpression(maxDepth-1);
				Term $term2 = randomExpression(maxDepth-1);
				return $func.apply($term1, $term2);
			}
			else {
				return terminals[randomIndex(terminals.length)];
			}
		}
	}

	// Genetic operations (mutation and crossover)

	private boolean mutation(Term $s, int maxDepth) {
		if (!$s.atomic()) {
			if (randomIndex(10) > 3) { // 70%
				$s.terms().set(randomIndex($s.terms().size()), randomExpression(maxDepth-1));
				return true;
			}
			else {
				Pt p = $t -> mutation($t, maxDepth-1);
				T t = $s::terms;
				// Fully symbolic with quotation to prevent evaluation.
				return (Boolean)$anyMatch.apply(Pt.$p.bind(p).quote(), T.$t.bind(t)).evaluate();
			}
		}
		return false;
	}

	private boolean crossover(Term $a, Term $b) { // maxDepth not necessary
		if (!$a.atomic() && !$b.atomic()) {
			if (randomIndex(10) > 3) { // 70%
				int i = randomIndex($a.terms().size());
				$a.terms().set(i, $b.terms().set(i, $a.terms().get(i)));
				return true;
			}
			else {
				Pt pa = $ta -> {
					Pt pb = $tb -> crossover($ta, $tb);
					return anyMatch(Pt.$p.bind(pb), $b.terms());
				};
				return anyMatch(Pt.$p.bind(pa), $a.terms());
			}
		}
		return false;
	}

	private static interface Pt {@Symbolic boolean p(Term t);} // Symbolic predicate of a term.
	private static interface T {@Symbolic Term.List t();}

	@Symbolic private static boolean anyMatch(MethodSymbol $p, Term.List terms) {
		return terms.stream().anyMatch($t->(Boolean)$p.apply($t.quote()).evaluate());
	}

	// Individual representation

	private static class Individual implements Comparable<Individual> {
		@Symbolic // Only for use in toString()
		public Double fitness;

		public final Term $expression;

		public Individual(Term $expression, Double fitness) {
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

		// TODO clone() not necessary since every individual is used only once for genetic operations.
	}

	public void run() {
		try {
			Fx f = x -> {this.x=x; return (Double)$target.evaluate();};
			Term $g = $integral.apply($LOWER_BOUND, $UPPER_BOUND, Fx.$f.bind(f).quote());

			System.out.println("Target: " + $target);
			System.out.println("|-" + $g + "=" + $g.evaluate());
			System.out.println("|-given: " +
				$a + "=" + a() + "," + $b + "=" + b() + "," + terminals[0] + "=" + A + "," +
				$LOWER_BOUND + "=" + LOWER_BOUND + "," + $UPPER_BOUND + "=" + UPPER_BOUND);

			Individual[] pop = new Individual[POP_SIZE];

			for (int i=0; i<POP_SIZE; i++) {
				Term $expr = randomExpression(MAX_DEPTH);
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
