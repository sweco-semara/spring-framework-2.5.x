/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.config;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessExceptionsException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rob Harrop
 */
public class AopNamespaceHandlerReturningTests extends TestCase {

	private ApplicationContext context;

	protected String getOKConfigLocation() {
		return "org/springframework/aop/config/aopNamespaceHandlerReturningOKTests.xml";
	}

	protected String getErrorConfigLocation() {
		return "org/springframework/aop/config/aopNamespaceHandlerReturningErrorTests.xml";
	}

	public void testReturningOnReturningAdvice() {
		this.context = new ClassPathXmlApplicationContext(getOKConfigLocation());
	}
	
	public void testParseReturningOnOtherAdviceType() {
		try {
			this.context = new ClassPathXmlApplicationContext(getErrorConfigLocation());
			fail("Expected BeanCreationException");
		} catch (BeanCreationException beanEx) {
			Throwable cause = beanEx.getCause();
			assertTrue("Expected PropertyAccessExceptionsException, got: " + cause.getClass(),
					cause instanceof PropertyAccessExceptionsException);
			PropertyAccessExceptionsException ex = (PropertyAccessExceptionsException) cause;
			PropertyAccessException nestedEx = ex.getPropertyAccessException("returningName");
			// We get back a MethodInvocationException, which nests what we really want to test...
			cause = nestedEx.getCause();
			assertTrue("Expected UnsupportedOperationException, got: " + cause.getClass(),
					cause instanceof UnsupportedOperationException);
			assertEquals("Only afterReturning advice can be used to bind a return value",
					cause.getMessage());
		}
	}

	protected ITestBean getTestBean() {
		return (ITestBean) this.context.getBean("testBean");
	}

}
