/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.MethodMatcher;
import org.springframework.lang.Nullable;

/**
 * 静态方法匹配的方便抽象超类，不需要关心运行时的参数。
 *
 * Convenient abstract superclass for static method matchers, which don't care
 * about arguments at runtime.
 *
 * @author Rod Johnson
 */
public abstract class StaticMethodMatcher implements MethodMatcher {

	@Override
	public final boolean isRuntime() {
		return false;
	}

	/**
	 * 静态匹配——带参数的匹配不支持。
	 *
	 * @param method the candidate method
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * @param args arguments to the method
	 * @return
	 */
	@Override
	public final boolean matches(Method method, @Nullable Class<?> targetClass, Object... args) {
		// should never be invoked because isRuntime() returns false
		throw new UnsupportedOperationException("Illegal MethodMatcher usage");
	}

}
