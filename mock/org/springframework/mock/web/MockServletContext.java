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

package org.springframework.mock.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.util.WebUtils;

/**
 * Mock implementation of the ServletContext interface.
 *
 * <p>Used for testing the Spring web framework; only rarely necessary for testing
 * application controllers. As long as application components don't explicitly
 * access the ServletContext, ClassPathXmlApplicationContext or
 * FileSystemXmlApplicationContext can be used to load the context files for testing,
 * even for DispatcherServlet context definitions.
 *
 * <p>For setting up a full WebApplicationContext in a test environment, you can
 * use XmlWebApplicationContext (or GenericWebApplicationContext), passing in an
 * appropriate MockServletContext instance. You might want to configure your
 * MockServletContext with a FileSystemResourceLoader in that case, to make your
 * resource paths interpreted as relative file system locations.
 *
 * <p>A common setup is to point your JVM working directory to the root of your
 * web application directory, in combination with filesystem-based resource loading.
 * This allows to load the context files as used in the web application, with
 * relative paths getting interpreted correctly. Such a setup will work with both
 * FileSystemXmlApplicationContext (which will load straight from the file system)
 * and XmlWebApplicationContext with an underlying MockServletContext (as long as
 * the MockServletContext has been configured with a FileSystemResourceLoader).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see #MockServletContext(org.springframework.core.io.ResourceLoader)
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 * @see org.springframework.web.context.support.GenericWebApplicationContext
 * @see org.springframework.context.support.ClassPathXmlApplicationContext
 * @see org.springframework.context.support.FileSystemXmlApplicationContext
 */
public class MockServletContext implements ServletContext {

	private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";


	private final Log logger = LogFactory.getLog(getClass());

	private final String resourceBasePath;
	
	private final ResourceLoader resourceLoader;

	private final Properties initParameters = new Properties();

	private final Hashtable attributes = new Hashtable();


	/**
	 * Create a new MockServletContext, using no base path and a
	 * DefaultResourceLoader (i.e. the classpath root as WAR root).
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public MockServletContext() {
		this("", null);
	}

	/**
	 * Create a new MockServletContext, using a DefaultResourceLoader.
	 * @param resourceBasePath the WAR root directory (should not end with a slash)
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public MockServletContext(String resourceBasePath) {
		this(resourceBasePath, null);
	}

	/**
	 * Create a new MockServletContext, using the specified ResourceLoader
	 * and no base path.
	 * @param resourceLoader the ResourceLoader to use (or null for the default)
	 */
	public MockServletContext(ResourceLoader resourceLoader) {
		this("", resourceLoader);
	}

	/**
	 * Create a new MockServletContext.
	 * @param resourceBasePath the WAR root directory (should not end with a slash)
	 * @param resourceLoader the ResourceLoader to use (or null for the default)
	 */
	public MockServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
		this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());

		// Use JVM temp dir as ServletContext temp dir.
		String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
		if (tempDir != null) {
			this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
		}
	}

	/**
	 * Build a full resource location for the given path,
	 * prepending the resource base path of this MockServletContext.
	 * @param path the path as specified
	 * @return the full resource path
	 */
	protected String getResourceLocation(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return this.resourceBasePath + path;
	}


	public ServletContext getContext(String name) {
		throw new UnsupportedOperationException("getContext");
	}

	public int getMajorVersion() {
		return 2;
	}

	public int getMinorVersion() {
		return 3;
	}

	public String getMimeType(String filePath) {
		throw new UnsupportedOperationException("getMimeType");
	}

	public Set getResourcePaths(String path) {
		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
		try {
			File file = resource.getFile();
			String[] fileList = file.list();
			String prefix = (path.endsWith("/") ? path : path + "/");
			Set resourcePaths = new HashSet(fileList.length);
			for (int i = 0; i < fileList.length; i++) {
				resourcePaths.add(prefix + fileList[i]);
			}
			return resourcePaths;
		}
		catch (IOException ex) {
			logger.info("Couldn't get resource paths for " + resource, ex);
			return null;
		}
	}

	public URL getResource(String path) throws MalformedURLException {
		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
		try {
			return resource.getURL();
		}
		catch (IOException ex) {
			logger.info("Couldn't get URL for " + resource, ex);
			return null;
		}
	}

	public InputStream getResourceAsStream(String path) {
		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
		try {
			return resource.getInputStream();
		}
		catch (IOException ex) {
			logger.info("Couldn't open InputStream for " + resource, ex);
			return null;
		}
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
		}
		return new MockRequestDispatcher(path);
	}

	public RequestDispatcher getNamedDispatcher(String path) {
		throw new UnsupportedOperationException("getNamedDispatcher");
	}

	public Servlet getServlet(String name) {
		throw new UnsupportedOperationException("getServlet");
	}

	public Enumeration getServlets() {
		throw new UnsupportedOperationException("getServlets");
	}

	public Enumeration getServletNames() {
		throw new UnsupportedOperationException("getServletNames");
	}

	public void log(String message) {
		logger.info(message);
	}

	public void log(Exception e, String message) {
		logger.info(message, e);
	}

	public void log(String message, Throwable t) {
		logger.info(message, t);
	}

	public String getRealPath(String path) {
		Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
		try {
			return resource.getFile().getAbsolutePath();
		}
		catch (IOException ex) {
			logger.info("Couldn't determine real path of resource " + resource, ex);
			return null;
		}
	}

	public String getServerInfo() {
		return "MockServletContext";
	}

	public String getInitParameter(String name) {
		return this.initParameters.getProperty(name);
	}

	public void addInitParameter(String name, String value) {
		this.initParameters.put(name, value);
	}

	public Enumeration getInitParameterNames() {
		return this.initParameters.keys();
	}

	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Enumeration getAttributeNames() {
		return this.attributes.keys();
	}

	public void setAttribute(String name, Object value) {
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			this.attributes.remove(name);
		}
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public String getServletContextName() {
		return "MockServletContext";
	}

}
