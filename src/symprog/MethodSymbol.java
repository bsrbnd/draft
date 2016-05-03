/*
 * Copyright 2015-2016 Bernard Blaser
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

/**
 * Symbolic view of methods.
 * 
 * @author Bernard Blaser
 *
 */
public class MethodSymbol extends Symbol<Method> {
	protected final String[] PARAMS;

	public MethodSymbol(String class_name, String name, String[] params) {
		super(class_name, name);
		PARAMS = params != null ? params : new String[0];
	}
	
	@Override
	public Method reflect() throws ClassNotFoundException, NoSuchMethodException {
		Class<?>[] params = new Class<?>[PARAMS.length];
		for (int i=0; i<PARAMS.length; i++) {
			params[i] = translate(PARAMS[i]);
		}
		return Class.forName(CLASS_NAME).getDeclaredMethod(NAME, params);
	}
	
	@Deprecated
	public Method reflectExplicit(Class<?>... params)
			throws ClassNotFoundException, NoSuchMethodException {
		return Class.forName(CLASS_NAME).getDeclaredMethod(NAME, params);
	}
	
	private Class<?> translate(String type) throws ClassNotFoundException {
		String trim = type.trim();
		
		if (trim.equals(boolean.class.getName().trim()))
			return boolean.class;
		else if (trim.equals(byte.class.getName().trim()))
			return byte.class;
		else if (trim.equals(char.class.getName().trim()))
			return char.class;
		else if (trim.equals(double.class.getName().trim()))
			return double.class;
		else if (trim.equals(float.class.getName().trim()))
			return float.class;
		else if (trim.equals(int.class.getName().trim()))
			return int.class;
		else if (trim.equals(long.class.getName().trim()))
			return long.class;
		else if (trim.equals(short.class.getName().trim()))
			return short.class;
		
		return Class.forName(trim);
	}

	@Override
	public Object evaluate(Object instance) throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		if (quoted) {return this;}

		Method m = reflect();
		m.setAccessible(true);
		return m.invoke(instance);
	}

	public ExpressionSymbol apply(Term... terms) {
		// TODO check expressions types with method parameters?
		return new ExpressionSymbol(CLASS_NAME, NAME, PARAMS, null, false, terms);
	}

	public BoundMethodSymbol bind(Object instance) {
		return new BoundMethodSymbol(CLASS_NAME, NAME, PARAMS, instance, true);
	}

	public static class BoundMethodSymbol extends MethodSymbol {
		protected final Object INSTANCE;
		protected final boolean BOUND; // false = later binding for the all expression

		private BoundMethodSymbol(String class_name, String name, String[] params, Object instance, boolean bound) {
			super(class_name, name, params);
			INSTANCE = instance;
			BOUND = bound;
		}

		@Override
		public ExpressionSymbol apply(Term... terms) {
			return new ExpressionSymbol(CLASS_NAME, NAME, PARAMS, INSTANCE, BOUND, terms);
		}

		@Override
		public Object evaluate(Object instance) throws ClassNotFoundException,
				NoSuchFieldException, NoSuchMethodException,
				IllegalAccessException, InvocationTargetException {
			return super.evaluate(BOUND ? INSTANCE : instance);
		}

		@Override
		public String toString() {
			String name = BOUND ? "@" + NAME : NAME;
			return quoted ? "'" + name : name;
		}
	}

	public static class ExpressionSymbol extends BoundMethodSymbol {
		private final Term[] TERMS;

		private ExpressionSymbol(String class_name, String name, String[] params, Object instance, boolean bound,
				Term... terms) {
			super(class_name, name, params, instance, bound);
			TERMS = terms != null ? terms : new Term[0];
		}

		@Override
		public List terms() {return new List(TERMS);}

		@Override
		public BoundMethodSymbol bind(Object instance) {
			return new ExpressionSymbol(CLASS_NAME, NAME, PARAMS, instance, true, TERMS);
		}

		@Override
		public Object evaluate(Object instance) throws ClassNotFoundException,
				NoSuchFieldException, NoSuchMethodException,
				IllegalAccessException, InvocationTargetException {

			if (quoted) {return this;}

			Object boundInstance = BOUND ? INSTANCE : instance;
			Object[] evaluations = new Object[TERMS.length];
			boolean[] quotations = new boolean[TERMS.length];

			for (int i=0; i<evaluations.length; i++) {
				evaluations[i] = TERMS[i].evaluate(boundInstance);
				quotations[i] = TERMS[i].quoted;
				TERMS[i].quoted = false;
			}

			Method m = reflect();
			m.setAccessible(true);
			Object result = m.invoke(boundInstance, evaluations);

			for (int i=0; i<quotations.length; i++) {
				TERMS[i].quoted = quotations[i];
			}
			return result;
		}

		@Override
		public String toString() {
			String name = super.toString();
			String sep = "(";
			for (int i=0; i<TERMS.length; i++) {
				name += sep + TERMS[i];
				sep = ",";
			}
			return TERMS.length > 0 ? name + ")" : name;
		}
	}
}
