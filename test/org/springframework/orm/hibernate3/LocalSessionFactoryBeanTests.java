/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.orm.hibernate3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.connection.UserSuppliedConnectionProvider;
import org.hibernate.engine.FilterDefinition;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Juergen Hoeller
 */
public class LocalSessionFactoryBeanTests extends TestCase {

	public void testLocalSessionFactoryBeanWithDataSource() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final List invocations = new ArrayList();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addInputStream(InputStream is) {
						try {
							is.close();
						}
						catch (IOException ex) {
						}
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setDataSource(ds);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertEquals("newSessionFactory", invocations.get(0));
	}

	public void testLocalSessionFactoryBeanWithTransactionAwareDataSource() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final List invocations = new ArrayList();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addInputStream(InputStream is) {
						try {
							is.close();
						}
						catch (IOException ex) {
						}
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(TransactionAwareDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setDataSource(ds);
		sfb.setUseTransactionAwareDataSource(true);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertEquals("newSessionFactory", invocations.get(0));
	}

	public void testLocalSessionFactoryBeanWithDataSourceAndMappingResources() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		MockControl tmControl = MockControl.createControl(TransactionManager.class);
		final TransactionManager tm = (TransactionManager) tmControl.getMock();
		final List invocations = new ArrayList();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addInputStream(InputStream is) {
						try {
							is.close();
						}
						catch (IOException ex) {
						}
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				assertEquals(LocalTransactionManagerLookup.class.getName(),
				    config.getProperty(Environment.TRANSACTION_MANAGER_STRATEGY));
				assertEquals(tm, LocalSessionFactoryBean.getConfigTimeTransactionManager());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingResources(new String[] {
			"/org/springframework/beans/factory/xml/test.xml",
			"/org/springframework/beans/factory/xml/child.xml"});
		sfb.setDataSource(ds);
		sfb.setJtaTransactionManager(tm);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertEquals("addResource", invocations.get(0));
		assertEquals("addResource", invocations.get(1));
		assertEquals("newSessionFactory", invocations.get(2));
	}

	public void testLocalSessionFactoryBeanWithDataSourceAndMappingJarLocations() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addJar(File file) {
						invocations.add("addResource " + file.getPath());
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingJarLocations(new Resource[] {
			new FileSystemResource("mapping.hbm.jar"), new FileSystemResource("mapping2.hbm.jar")});
		sfb.setDataSource(ds);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertTrue(invocations.contains("addResource mapping.hbm.jar"));
		assertTrue(invocations.contains("addResource mapping2.hbm.jar"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithDataSourceAndProperties() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addInputStream(InputStream is) {
						try {
							is.close();
						}
						catch (IOException ex) {
						}
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				assertEquals("myValue", config.getProperty("myProperty"));
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingLocations(new Resource[] {
			new ClassPathResource("/org/springframework/beans/factory/xml/test.xml")});
		sfb.setDataSource(ds);
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, "myClass");
		prop.setProperty("myProperty", "myValue");
		sfb.setHibernateProperties(prop);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertTrue(invocations.contains("addResource"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithValidProperties() throws Exception {
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(UserSuppliedConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals("myValue", config.getProperty("myProperty"));
				invocations.add("newSessionFactory");
				return null;
			}
		};
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, UserSuppliedConnectionProvider.class.getName());
		prop.setProperty("myProperty", "myValue");
		sfb.setHibernateProperties(prop);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithInvalidProperties() throws Exception {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setMappingResources(new String[0]);
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, "myClass");
		sfb.setHibernateProperties(prop);
		try {
			sfb.afterPropertiesSet();
		}
		catch (HibernateException ex) {
			// expected, provider class not found
		}
	}

	public void testLocalSessionFactoryBeanWithInvalidMappings() throws Exception {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setMappingResources(new String[] {"mapping.hbm.xml"});
		try {
			sfb.afterPropertiesSet();
		}
		catch (IOException ex) {
			// expected, mapping resource not found
		}
	}

	public void testLocalSessionFactoryBeanWithCustomSessionFactory() throws Exception {
		MockControl factoryControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sessionFactory = (SessionFactory) factoryControl.getMock();
		sessionFactory.close();
		factoryControl.setVoidCallable(1);
		factoryControl.replay();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) {
				return sessionFactory;
			}
		};
		sfb.setMappingResources(new String[0]);
		sfb.setDataSource(new DriverManagerDataSource());
		sfb.afterPropertiesSet();
		assertTrue(sessionFactory.equals(sfb.getObject()));
		sfb.destroy();
		factoryControl.verify();
	}

	public void testLocalSessionFactoryBeanWithEntityInterceptor() throws Exception {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration setInterceptor(Interceptor interceptor) {
						throw new IllegalArgumentException(interceptor.toString());
					}
				};
			}
		};
		sfb.setMappingResources(new String[0]);
		sfb.setDataSource(new DriverManagerDataSource());
		MockControl interceptorControl = MockControl.createControl(Interceptor.class);
		Interceptor entityInterceptor = (Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		sfb.setEntityInterceptor(entityInterceptor);
		try {
			sfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("Correct exception", ex.getMessage().equals(entityInterceptor.toString()));
		}
	}

	public void testLocalSessionFactoryBeanWithNamingStrategy() throws Exception {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration setNamingStrategy(NamingStrategy namingStrategy) {
						throw new IllegalArgumentException(namingStrategy.toString());
					}
				};
			}
		};
		sfb.setMappingResources(new String[0]);
		sfb.setDataSource(new DriverManagerDataSource());
		sfb.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
		try {
			sfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("Correct exception", ex.getMessage().equals(ImprovedNamingStrategy.INSTANCE.toString()));
		}
	}

	public void testLocalSessionFactoryBeanWithEventListeners() throws Exception {
		final Map registeredListeners = new HashMap();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public void setListener(String type, Object listener) {
						registeredListeners.put(type, listener);
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				return null;
			}
		};
		sfb.setMappingResources(new String[0]);
		sfb.setDataSource(new DriverManagerDataSource());
		Map listeners = new HashMap();
		listeners.put("flush", "myListener");
		listeners.put("create", "yourListener");
		sfb.setEventListeners(listeners);
		sfb.afterPropertiesSet();
		assertEquals(listeners, registeredListeners);
	}

	public void testLocalSessionFactoryBeanWithFilterDefinitions() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("filterDefinitions.xml", getClass()));
		FilterTestLocalSessionFactoryBean sf = (FilterTestLocalSessionFactoryBean) xbf.getBean("&sessionFactory");
		assertEquals(2, sf.registeredFilterDefinitions.size());
		FilterDefinition filter1 = (FilterDefinition) sf.registeredFilterDefinitions.get(0);
		FilterDefinition filter2 = (FilterDefinition) sf.registeredFilterDefinitions.get(1);

		assertEquals("filter1", filter1.getFilterName());
		assertEquals(2, filter1.getParameterNames().size());
		assertEquals(Hibernate.STRING, filter1.getParameterType("param1"));
		assertEquals(Hibernate.LONG, filter1.getParameterType("otherParam"));
		assertEquals("someCondition", filter1.getDefaultFilterCondition());

		assertEquals("filter2", filter2.getFilterName());
		assertEquals(1, filter2.getParameterNames().size());
		assertEquals(Hibernate.INTEGER, filter2.getParameterType("myParam"));
	}


	public static class FilterTestLocalSessionFactoryBean extends LocalSessionFactoryBean {

		public List registeredFilterDefinitions = new LinkedList();

		protected Configuration newConfiguration() throws HibernateException {
			return new Configuration() {
				public void addFilterDefinition(FilterDefinition definition) {
					registeredFilterDefinitions.add(definition);
				}
			};
		}
		protected SessionFactory newSessionFactory(Configuration config) {
			return null;
		}
	}

}
