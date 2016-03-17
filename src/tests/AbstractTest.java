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

import symprog.*;
import java.lang.reflect.*;

public abstract class AbstractTest {
	protected final String MY_FIELD="myField";
	protected final String MY_METHOD="myMethod";

	private int nb = 0;

	protected abstract void run() throws Exception;

	protected void check(Symbol s, String name, AccessibleObject reflect) throws Exception {
		boolean ok = s.toString().equals(name) && s.reflect().equals(reflect);
		if (!ok) throw new RuntimeException("Error with symbol: " + s);
		nb++;
	}

	protected void runTests() {
		try {
			run();

			System.out.println("All tests passed (count=" + nb + ").");
		}
		catch (Exception e) {
			throw new RuntimeException("Error running the tests: " + e.getMessage());
		}
	}
}
