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
 * @summary Erasure tests
 * @author Bernard Blaser
 *
 * @compile -processor symprog.SymProc ErasureTests.java
 * @run main ErasureTests
 */

import symprog.*;
import java.lang.reflect.*;
import java.util.*;

// JLS8 4.4
class MyTypeVariable {
	public static class C {}
	public static interface I {}

	@Symbolic private C myField;
	@Symbolic private <T extends C & I> void myMethod(T t, T[] u) {}
}

// JLS8 4.5
class MyParameterizedType<T extends Number> {
	@Symbolic private T myField;
	@Symbolic private void myMethod(T param, Collection<? extends Number> c1, Collection<? super Number>[] c2) {}
}

public class ErasureTests extends AbstractTest {
	public static void main(String[] args) {
		new ErasureTests().runTests();
	}

	@Override
	protected void run() throws Exception {
		check(MyTypeVariable.$myField, MY_FIELD, MyTypeVariable.class.getDeclaredField(MY_FIELD));
		check(MyTypeVariable.$myMethod, MY_METHOD, MyTypeVariable.class.getDeclaredMethod(MY_METHOD,
				MyTypeVariable.C.class, MyTypeVariable.C[].class));

		check(MyParameterizedType.$myField, MY_FIELD, MyParameterizedType.class.getDeclaredField(MY_FIELD));
		check(MyParameterizedType.$myMethod, MY_METHOD, MyParameterizedType.class.getDeclaredMethod(MY_METHOD,
				Number.class, Collection.class, Collection[].class));
	}
}
