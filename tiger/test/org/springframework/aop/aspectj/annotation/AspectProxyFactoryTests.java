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

package org.springframework.aop.aspectj.annotation;

import junit.framework.TestCase;
import org.springframework.aop.aspectj.autoproxy.MultiplyReturnValue;
import org.springframework.aop.aspectj.autoproxy.PerThisAspect;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.test.AssertThrows;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class AspectProxyFactoryTests extends TestCase {

	public void testWithNonAspect() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new TestBean());
				proxyFactory.addAspect(TestBean.class);
			}
		}.runTest();
	}

	public void testWithSimpleAspect() throws Exception {                     
		TestBean bean = new TestBean();
		bean.setAge(2);
		AspectJProxyFactory proxyFactory = new AspectJProxyFactory(bean);
		proxyFactory.addAspect(MultiplyReturnValue.class);
		ITestBean proxy = proxyFactory.getProxy();
		assertEquals("Multiplication did not occur", bean.getAge() * 2, proxy.getAge());
	}

	public void testWithPerThisAspect() throws Exception {
		TestBean bean1 = new TestBean();
		TestBean bean2 = new TestBean();

		AspectJProxyFactory pf1 = new AspectJProxyFactory(bean1);
		pf1.addAspect(PerThisAspect.class);

		AspectJProxyFactory pf2 = new AspectJProxyFactory(bean2);
		pf2.addAspect(PerThisAspect.class);

		ITestBean proxy1 = pf1.getProxy();
		ITestBean proxy2 = pf2.getProxy();

		assertEquals(0, proxy1.getAge());
		assertEquals(1, proxy1.getAge());
		assertEquals(0, proxy2.getAge());
		assertEquals(2, proxy1.getAge());
	}
}
