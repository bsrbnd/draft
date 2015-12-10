/*
 * Copyright 2015 Bernard Blaser
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
import java.lang.reflect.*;

abstract class Table<T extends Number> {
	@Symbolic
	private T id;

	@Symbolic
	private void setId(T id) {setId(id, true);}

	// User defined symbol's name to avoid conflit in symbolic overloaded methods.
	@Symbolic(value="_", suffix="CheckNull")
	private void setId(T id, boolean checkNull) {
		if (checkNull && id == null) throw new NullPointerException($id + " can't be null in " + _setIdCheckNull + "()");
		this.id = id;
	}

	public T getId() {return id;}
}

public class Example {
	private static class MyTable extends Table<Integer> {
		public static final String NAME = MyTable.class.getSimpleName().toLowerCase();

		@Symbolic
		private Integer value;

		public void setValue(Integer value) {this.value = value != null ? value : 0;}
		public Integer getValue() {return value;}
	}

	public static void main(String[] args) {
		final int ID = 1;

		System.out.println("Request: " +
			"SELECT " + MyTable.$id + ", " + MyTable.$value +
			" FROM " + MyTable.NAME +
			" WHERE " + MyTable.$id + "=" + ID);

		try {
			MyTable row = new MyTable();

			// row.setId(ID); // ERROR: PRIVATE ACCESS
			Method m = row.$setId.reflect();
			m.setAccessible(true);
			m.invoke(row, ID);
			row.setValue(4);

			System.out.println("Result: " +
				MyTable.$id + "=" + row.getId() + ", " +
				MyTable.$value + "=" + row.getValue());

			// NullPointerException
			m = row._setIdCheckNull.reflect();
			m.setAccessible(true);
			m.invoke(row, null, true);
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getCause() != null ? e.getCause() : e;
			System.err.println("Invoke exception: " + t.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
