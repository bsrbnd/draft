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
 * Symbolic view of fields.
 * 
 * @author Bernard Blaser
 *
 */
public class FieldSymbol extends Symbol<Field> {
	public FieldSymbol(String class_name, String name) {
		super(class_name, name);
	}
	
	@Override
	public Field reflect() throws ClassNotFoundException, NoSuchFieldException {
		return Class.forName(CLASS_NAME).getDeclaredField(NAME);
	}

	@Override
	public Object evaluate(Object instance) throws SymbolicException {
		if (quoted) {return this;}

		try {
			Field f = reflect();
			f.setAccessible(true);
			return f.get(instance);
		}
		catch (Exception e) {throw new SymbolicException(e);}
	}

	public BoundFieldSymbol bind(Object instance) {
		return new BoundFieldSymbol(CLASS_NAME, NAME, instance);
	}

	public static class BoundFieldSymbol extends FieldSymbol {
		private final Object INSTANCE;

		private BoundFieldSymbol(String class_name, String name, Object instance) {
			super(class_name, name);
			INSTANCE = instance;
		}

		@Override
		public Object evaluate(Object instance) throws SymbolicException {
			return super.evaluate(INSTANCE);
		}

		@Override
		public String toString() {
			String name = "@" + NAME;
			return quoted ? "'" + name : name;
		}
	}
}
