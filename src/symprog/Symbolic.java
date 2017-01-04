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

import java.lang.annotation.*;
import java.lang.reflect.Modifier;

/**
 * Provides symbolic access to class members.
 * 
 * @author Bernard Blaser
 *
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Symbolic {
	String value() default "$";
	String suffix() default "";

	int DEFAULT = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
	int SAME = 0;
	int flags() default DEFAULT; // Any java.lang.reflect.Modifier, see JVMS 4.5/4.6

	String origin() default ""; // Flat class name
}
