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
package symprog;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Symbolic expression's term.
 * 
 * @author Bernard Blaser
 *
 */
public abstract class Term implements Cloneable {
	public static class List {
		public static final List NIL = new List(new Term[0]);

		private final Term[] TERMS;
		protected List(Term... terms) {TERMS=terms;}

		public int size() {return TERMS.length;}
		// Fast (imperative) indexed terms' access.
		public Term get(int i) {return TERMS[i];}
		public Term set(int i, Term t) { // Returns the old Term
			Term old=TERMS[i];
			TERMS[i]=t;
			return old;
		}
		public void set(Term... terms) {
			for (int i=0; i<TERMS.length && i<terms.length; i++)
				TERMS[i] = terms[i];
		}

		// Iterative or functional terms' access (stream doesn't need to be closed).
		public Stream<Term> stream() {
			return Arrays.stream(TERMS);
		}
		public void set(Stream<Term> stream) {
			set(stream.toArray(l->new Term[l]));
		}
	}

	public boolean atomic() {return terms().size() == 0;}
	public List terms() {return List.NIL;}

	protected boolean quoted = false; // Prevents evaluation
	public Term quote() throws SymbolicException {
		try {
			Term t = (Term)clone();
			t.quoted=true;
			return t;
		}
		catch (Exception e) {throw new SymbolicException(e);}
	}

	public abstract Object evaluate(Object instance) throws SymbolicException;

	public Object evaluate() throws SymbolicException {
		return evaluate(null);
	}
}
