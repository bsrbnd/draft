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

/*
 * @test
 * @summary Basic tests
 * @author Bernard Blaser
 *
 * @compile -processor symprog.SymProc BasicTests.java
 * @run main BasicTests
 */

import symprog.*;
import java.lang.reflect.*;
import java.util.*;

// JLS8 8.1.1
class MyClass {
	@Symbolic private Object myField;
	@Symbolic private void myMethod(Object param) {}
}

// JLS8 10.2
class MyArrays {
	@Symbolic private int[] myField;
	@Symbolic private void myMethod(int[] param) {}
}

// JLS8 8.1.3, nested but not inner
class MyOuter {
	public static class MyNested {
		@Symbolic private MyNested myField;
		@Symbolic private void myMethod(MyNested param, MyNested[] params) {}
	}
}

// JLS8 8.9
class MyEnum {
	public enum Value {A, B, C}

	@Symbolic private Value myField;
	@Symbolic private Value myMethod(Value v1, Value[] v2) {return Value.A;}
}

// JLS8 9.1
class MyInterface {
	public static interface I<T extends Number> {}

	@Symbolic private I<? extends Number> myField;
	@Symbolic private I<? extends Number> myMethod(I<?> param, I<? super Number>[] params) {return null;}
}

public class BasicTests extends AbstractTest {
	public static void main(String[] args) {
		new BasicTests().runTests();
	}

	@Override
	protected void run() throws Exception {
		check(MyClass.$myField, MY_FIELD, MyClass.class.getDeclaredField(MY_FIELD));
		check(MyClass.$myMethod, MY_METHOD, MyClass.class.getDeclaredMethod(MY_METHOD, Object.class));

		check(MyArrays.$myField, MY_FIELD, MyArrays.class.getDeclaredField(MY_FIELD));
		check(MyArrays.$myMethod, MY_METHOD, MyArrays.class.getDeclaredMethod(MY_METHOD, int[].class));

		check(MyOuter.MyNested.$myField, MY_FIELD, MyOuter.MyNested.class.getDeclaredField(MY_FIELD));
		check(MyOuter.MyNested.$myMethod, MY_METHOD, MyOuter.MyNested.class.getDeclaredMethod(MY_METHOD,
				MyOuter.MyNested.class, MyOuter.MyNested[].class));

		check(MyEnum.$myField, MY_FIELD, MyEnum.class.getDeclaredField(MY_FIELD));
		check(MyEnum.$myMethod, MY_METHOD, MyEnum.class.getDeclaredMethod(MY_METHOD,
				MyEnum.Value.class, MyEnum.Value[].class));

		check(MyInterface.$myField, MY_FIELD, MyInterface.class.getDeclaredField(MY_FIELD));
		check(MyInterface.$myMethod, MY_METHOD, MyInterface.class.getDeclaredMethod(MY_METHOD,
				MyInterface.I.class, MyInterface.I[].class));
	}
}
