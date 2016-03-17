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
 * Symbolic view of class members.
 * 
 * @author Bernard Blaser
 *
 */
public abstract class Symbol<T extends AccessibleObject> {
	protected final String CLASS_NAME;
	protected final String NAME;
	protected final String[] PARAMS;
	
	protected Symbol(String class_name, String name, String[] params) {
		CLASS_NAME = class_name;
		NAME = name;
		PARAMS = params;
	}
	
	protected Symbol(String class_name, String name) {
		this(class_name, name, null);
	}
	
	@Override
	public String toString() {return NAME;}
	
	public abstract T reflect() throws Exception;

	public abstract Object evaluate(Object instance) throws ClassNotFoundException, NoSuchFieldException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException;
}
