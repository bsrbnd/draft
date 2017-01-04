/*
 * Copyright 2015-2017 Bernard Blaser
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

import java.lang.reflect.Modifier;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.jvm.ClassReader;

/**
 * Annotations processor that provides symbolic access to class members.
 * 
 * @author Bernard Blaser
 *
 */
@SupportedAnnotationTypes("symprog.Symbolic")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SymProc extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment re) {
		if (!re.processingOver()) {
			SymbolGenerator gen = new SymbolGenerator(
					((JavacProcessingEnvironment) processingEnv).getContext()
			);

			for (Element e: re.getRootElements()) {
				JCTree tree = (JCTree) Trees.instance(processingEnv).getTree(e);
				tree.accept(gen);
			}
		}
		return true;
	}

	private static class SymbolGenerator extends TreeTranslator {
		private TreeMaker nodes;
		private Names names;
		private Types types;
		private Symtab symtab;
		private ClassReader classes;

		private SymbolGenerator(Context context) {
			nodes = TreeMaker.instance(context);
			names = Names.instance(context);
			types = Types.instance(context);
			symtab = Symtab.instance(context);
			classes = ClassReader.instance(context);
		}

		@Override
		public void visitClassDef(JCClassDecl tree) {
			super.visitClassDef(tree);
			
			if (tree.sym != null) {
				// Not anonymous
				System.out.println("Generate symbols for class: " +
				tree.sym.flatname);
				
				generateSymbols(tree);
			}
		}

		private void generateSymbols(JCClassDecl clazz) {
			List<JCTree> newDefs = List.from(clazz.defs);
			
			for (JCTree decl: clazz.defs) {
				if (decl instanceof JCMethodDecl) {
					JCMethodDecl met = (JCMethodDecl) decl;
					// TODO remove annotation after processing?
					Symbolic symbolic = met.sym.getAnnotation(Symbolic.class);
					Name name = met.name;
					
					if (symbolic != null) {
						// TODO compiler output?
						System.out.println("|-" + name + " -> " + symbolic.value()+name+symbolic.suffix());
					
						List<String> paramsTypes = List.nil();
					
						for (VarSymbol param: met.sym.getParameters()) {
							Type erasure = param.erasure(types);
							paramsTypes = paramsTypes.append(translate(erasure));
						}
						
						newDefs = newDefs.append(generateSymbol(
								MethodSymbol.class.getName(),
								symbolic,
								clazz.sym.flatname.toString(),
								name.toString(),
								paramsTypes));
					}
				}
				else if (decl instanceof JCVariableDecl) {
					JCVariableDecl var = (JCVariableDecl) decl;
					Symbolic symbolic = var.sym.getAnnotation(Symbolic.class);
					Name name = var.name;
					
					if (symbolic != null) {
						System.out.println("|-" + name + " -> " + symbolic.value()+name+symbolic.suffix());
						
						newDefs = newDefs.append(generateSymbol(
								FieldSymbol.class.getName(),
								symbolic,
								clazz.sym.flatname.toString(),
								name.toString(),
								null));
					}
					
				}
			}
			clazz.defs = newDefs;
		}

		/**
		 * Generates a symbolic field declaration representing a member (field or method).<br>
		 * 
		 * @param symbolTypeName : generated symbolic field's flat type name
		 * (<b><code>symprog.FieldSymbol</code></b> or <b><code>symprog.MethodSymbol</code></b>)
		 * @param symbolic : member's symbolic annotation
		 * @param className : member's flat class name
		 * (ex. <b><code>mypackage.MyClass$MyInner</code></b>)
		 * @param name : member's name
		 * @param params : method's parameter types; compatible with <b><code>Class.forName()</code></b>,
		 * <b><code>null</code></b> for fields
		 * 
		 * @return symbolic field's declaration
		 */
		private JCVariableDecl generateSymbol(
				String symbolTypeName,
				Symbolic symbolic,
				String className,
				String name,
				List<String> params
		) {
			JCExpression[] symbolParams = new JCExpression[] {
					nodes.Literal(className),
					nodes.Literal(name)
			};
			List<JCExpression> paramslist = List.from(symbolParams);
			
			if(params != null) {
				List<JCExpression> erasures = List.nil();
				
				for (String param: params) {
					erasures = erasures.append(nodes.Literal(param));
				}
				
				paramslist = paramslist.append(nodes.NewArray(
						nodes.Type(symtab.stringType),
						List.nil(),
						erasures));
			}
			
			ClassSymbol cs = classes.loadClass(names.fromString(symbolTypeName));
			JCExpression symbolType = nodes.QualIdent(cs);
			
			JCExpression newSymbol = nodes.NewClass(
					null, null,
					symbolType,
					paramslist,
					null
			);
			
			return nodes.VarDef(
					nodes.Modifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
					names.fromString(symbolic.value() + name + symbolic.suffix()),
					symbolType,
					newSymbol);
		}

		private String translate(Type type) {
			String trim = "";
			
			if (type instanceof Type.ArrayType) {
				Type elem = type;
				while (elem instanceof Type.ArrayType) {
					elem = ((Type.ArrayType)elem).elemtype;
					trim += "[";
				}
				trim += arrayName(elem.tsym.flatName().toString().trim());
			}
			else {
				trim = type.tsym.flatName().toString().trim();
			}
			
			System.out.println(" |-" + trim);
			
			return trim;
		}

		private String arrayName(String type) {
			if (type.equals(boolean.class.getName().trim()))
				return "Z";
			else if (type.equals(byte.class.getName().trim()))
				return "B";
			else if (type.equals(char.class.getName().trim()))
				return "C";
			else if (type.equals(double.class.getName().trim()))
				return "D";
			else if (type.equals(float.class.getName().trim()))
				return "F";
			else if(type.equals(int.class.getName().trim()))
				return "I";
			else if (type.equals(long.class.getName().trim()))
				return "J";
			else if (type.equals(short.class.getName().trim()))
				return "S";
			else return "L" + type + ";";
		}
	}
}
