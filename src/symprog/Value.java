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

/**
 * Symbolic expression's captured value in the execution context.
 * 
 * @author Bernard Blaser
 *
 */
public class Value extends Term {
	private final Object VALUE;

	public Value(Object value) {VALUE = value;}

	@Override
	public Object evaluate(Object instance) throws ClassNotFoundException, NoSuchFieldException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (quoted) {return this;}

		return VALUE;
	}

	@Override
	public String toString() {return quoted ? "'#" : "#";}
}
