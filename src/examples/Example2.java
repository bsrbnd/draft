/*
 * Copyright 2016-2017 Bernard Blaser
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
import java.util.*;

/**
 * Mutable database queries.
 * 
 * @author Bernard Blaser
 *
 */
public class Example2 {
	private static class MyTable {
		@Symbolic("_")
		private Integer id;

		@Symbolic("_")
		private Integer val;

		protected MyTable() {super();}
	}

	public static void main(String[] args) {
		try {
			// Business layer.
			List<MyTable> rows = fetch(MyTable.class,
				_AND.build(
					_OR.build(
						_EQ.build(MyTable._id.quote(), value(10)),
						_EQ.build(MyTable._id.quote(), value(20))
					),
					_EQ.build(MyTable._val.quote(), value(30))
				)
			);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Presistence layer.
	static <R> List<R> fetch(Class<R> table, Term filter) throws Exception {
		Term prefix = value("t");

		// Add prefix to filter tree (= query mutation).
		class T {
			Term dot(Term t) {
				if (t instanceof FieldSymbol)
					return _DOT.build(prefix, t).quote();
				else
					t.terms().set(t.terms().stream().map(this::dot));

				return t;
			}
		}

		filter.terms().set(filter.terms().stream().map(new T()::dot));

		// Mutable query tree.
		Term tree = _SELECT.build(
			_COLS.build(terms(
				_ALIAS.build(
					_DOT.build(prefix, MyTable._id.quote()),
					MyTable._id.quote()
				),
				_ALIAS.build(
					_DOT.build(prefix, MyTable._val.quote()),
					MyTable._val.quote()
				)
			)),
			_FROM.build(
				_AS.build(value(table), prefix)
			),
			_WHERE.build(filter)
		);

		// Generate the query from the syntactic tree.
		String query = (String)tree.evaluate();
		System.out.println(query);

		// Execute the query and fetch the result.
		List<R> rows = new ArrayList<R>();
		// for (Result result: execute(query)) {
			R row = table.newInstance();
			// fill(row, result);
			rows.add(row);
		// }
		return rows;
	}

	@Symbolic("_")
	static String SELECT(String columns, String from, String where) {
		return _SELECT + " " + columns + "\n  " + from + "\n " + where;
	}

	@Symbolic("_")
	static String COLS(Term... cols) {
		String ql = "", sep = "";
		for (Term col: cols) {
			ql += sep + col.evaluate();
			sep = ", ";
		}
		return ql;
	}

	@Symbolic("_")
	static String ALIAS(String c, Term alias) {
		return c + " \"" + alias + "\"";
	}

	@Symbolic("_")
	static String DOT(String prefix, Term c) {
		return prefix + "." + c;
	}

	@Symbolic("_")
	static String FROM(String as) {
		return _FROM + " " + as;
	}

	@Symbolic("_")
	static String AS(Class table, String prefix) {
		return table.getSimpleName() + " " + _AS + " "  + prefix;
	}

	@Symbolic("_")
	static String WHERE(String cond) {
		return _WHERE + " " + cond;
	}

	@Symbolic("_")
	static String OR(String expr1, String expr2) {
		return "(" + expr1 + " " + _OR + " " + expr2 + ")";
	}

	@Symbolic("_")
	static String AND(String expr1, String expr2) {
		return "(" + expr1 + " " + _AND + " " + expr2 + ")";
	}

	@Symbolic("_")
	static String EQ(Term c, Object val) {
		if (c instanceof FieldSymbol)
			return c + "=" + val;
		else
			return c.evaluate() + "=" + val;
	}

	static Value value(Object v) { return new Value(v); }
	static Value terms(Term... terms) { return new Value(terms); }
}
