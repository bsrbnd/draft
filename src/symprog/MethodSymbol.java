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
		Method m = reflect();
		m.setAccessible(true);
		return m.invoke(instance);
	}

	public AppliedSymbol apply(Symbol<?>... expressions) {
		// TODO check expressions types with method parameters?
		return new AppliedSymbol(CLASS_NAME, NAME, PARAMS, expressions);
	}

	public static class AppliedSymbol extends MethodSymbol {
		public final Symbol<?>[] EXPRESSIONS;

		private AppliedSymbol(String class_name, String name, String[] params, Symbol<?>... expressions) {
			super(class_name, name, params);
			EXPRESSIONS = expressions != null ? expressions : new Symbol<?>[0];
		}

		@Override
		public Object evaluate(Object instance) throws ClassNotFoundException,
				NoSuchFieldException, NoSuchMethodException,
				IllegalAccessException, InvocationTargetException {

			Object[] evaluations = new Object[EXPRESSIONS.length];
			for (int i=0; i<evaluations.length; i++) {
				evaluations[i] = EXPRESSIONS[i].evaluate(instance);
			}

			Method m = reflect();
			m.setAccessible(true);
			return m.invoke(instance, evaluations);
		}

		@Override
		public String toString() {
			String name = NAME;
			String sep = "(";
			for (int i=0; i<EXPRESSIONS.length; i++) {
				name += sep + EXPRESSIONS[i];
				sep = ",";
			}
			return EXPRESSIONS.length > 0 ? name + ")" : name;
		}
	}
}
