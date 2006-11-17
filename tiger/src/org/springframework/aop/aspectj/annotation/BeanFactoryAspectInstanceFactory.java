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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.Ordered;

/**
 * AspectInstanceFactory backed by a Spring
 * {@link org.springframework.beans.factory.BeanFactory}.
 *
 * <p>Note that this may instantiate multiple times if using a prototype,
 * which probably won't give the semantics you expect. 
 * Use a {@link LazySingletonAspectInstanceFactoryDecorator}
 * to wrap this to ensure only one new aspect comes back.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 * @see LazySingletonAspectInstanceFactoryDecorator
 */
public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory {

	private final BeanFactory beanFactory;

	private final String name;

	private final AspectMetadata aspectMetadata;


	/**
	 * Create a BeanFactoryAspectInstance factory. AspectJ will be called to
	 * introspect to create AJType metadata using the type returned for the given bean name
	 * from the BeanFactory. 
	 * @param beanFactory BeanFactory to obtain instance(s) from
	 * @param name name of the bean
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name) {
		this(beanFactory, name, beanFactory.getType(name));
	}
	
	/**
	 * Create a BeanFactoryAspectInstance factory, providing a type that AspectJ should
	 * introspect to create AJType metadata. Use if the BeanFactory may consider the type
	 * to be a subclass (as when using CGLIB), and the information should relate to a superclass.
	 * @param beanFactory BeanFactory to obtain instance(s) from
	 * @param name the name of the bean
	 * @param type the type that should be introspected by AspectJ
	 */
	public BeanFactoryAspectInstanceFactory(BeanFactory beanFactory, String name, Class type) {
		this.beanFactory = beanFactory;
		this.name = name;
		this.aspectMetadata = new AspectMetadata(type, name);
	}


	public Object getAspectInstance() {
		return this.beanFactory.getBean(this.name);
	}

	public AspectMetadata getAspectMetadata() {
		return this.aspectMetadata;
	}

	public int getOrder() {
		if (this.beanFactory.isSingleton(this.name) &&
				this.beanFactory.isTypeMatch(this.name, Ordered.class)) {
			return ((Ordered) this.beanFactory.getBean(this.name)).getOrder();
		}
		return Ordered.LOWEST_PRECEDENCE;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + ": bean name '" + this.name + "'";
	}

}
