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

package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 启动监听器：启动或关闭spring应用上下文容器。
 *
 * 这个类实现了`ServletContextListener`，所以配置在`web.xml`中的servlet上下文会被tomcat初始化，
 * 接着{@link ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)}方法会被调用，
 * 然后就会由spring这个{@link ContextLoaderListener}来负责spring容器的启动。
 *
 * 由`ContextLoaderListener`启动的web应用是根上下文、也就是整个spring容器，定义了一堆服务bean的容器。
 * springMvc存放控制器和相关请求映射的上下文，以spring容器作为父上下文。
 *
 * 因为实现了{@link ServletContextListener}接口，所以servlet容器Context的任何变化都会被监听调用，如contextInitialized、contextDestroyed方法。
 *
 * Bootstrap listener to start up and shut down Spring's root {@link WebApplicationContext}.
 * Simply delegates to {@link ContextLoader} as well as to {@link ContextCleanupListener}.
 *
 * <p>As of Spring 3.1, {@code ContextLoaderListener} supports injecting the root web
 * application context via the {@link #ContextLoaderListener(WebApplicationContext)}
 * constructor, allowing for programmatic configuration in Servlet 3.0+ environments.
 * See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 17.02.2003
 * @see #setContextInitializers
 * @see org.springframework.web.WebApplicationInitializer
 */
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

	/**
	 * Create a new {@code ContextLoaderListener} that will create a web application
	 * context based on the "contextClass" and "contextConfigLocation" servlet
	 * context-params. See {@link ContextLoader} superclass documentation for details on
	 * default values for each.
	 * <p>This constructor is typically used when declaring {@code ContextLoaderListener}
	 * as a {@code <listener>} within {@code web.xml}, where a no-arg constructor is
	 * required.
	 * <p>The created application context will be registered into the ServletContext under
	 * the attribute name {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE}
	 * and the Spring application context will be closed when the {@link #contextDestroyed}
	 * lifecycle method is invoked on this listener.
	 * @see ContextLoader
	 * @see #ContextLoaderListener(WebApplicationContext)
	 * @see #contextInitialized(ServletContextEvent)
	 * @see #contextDestroyed(ServletContextEvent)
	 */
	public ContextLoaderListener() {
	}

	/**
	 * Create a new {@code ContextLoaderListener} with the given application context. This
	 * constructor is useful in Servlet 3.0+ environments where instance-based
	 * registration of listeners is possible through the {@link javax.servlet.ServletContext#addListener}
	 * API.
	 * <p>The context may or may not yet be {@linkplain
	 * org.springframework.context.ConfigurableApplicationContext#refresh() refreshed}. If it
	 * (a) is an implementation of {@link ConfigurableWebApplicationContext} and
	 * (b) has <strong>not</strong> already been refreshed (the recommended approach),
	 * then the following will occur:
	 * <ul>
	 * <li>If the given context has not already been assigned an {@linkplain
	 * org.springframework.context.ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * <li>{@link #customizeContext} will be called</li>
	 * <li>Any {@link org.springframework.context.ApplicationContextInitializer ApplicationContextInitializer org.springframework.context.ApplicationContextInitializer ApplicationContextInitializers}
	 * specified through the "contextInitializerClasses" init-param will be applied.</li>
	 * <li>{@link org.springframework.context.ConfigurableApplicationContext#refresh refresh()} will be called</li>
	 * </ul>
	 * If the context has already been refreshed or does not implement
	 * {@code ConfigurableWebApplicationContext}, none of the above will occur under the
	 * assumption that the user has performed these actions (or not) per his or her
	 * specific needs.
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * <p>In any case, the given application context will be registered into the
	 * ServletContext under the attribute name {@link
	 * WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE} and the Spring
	 * application context will be closed when the {@link #contextDestroyed} lifecycle
	 * method is invoked on this listener.
	 * @param context the application context to manage
	 * @see #contextInitialized(ServletContextEvent)
	 * @see #contextDestroyed(ServletContextEvent)
	 */
	public ContextLoaderListener(WebApplicationContext context) {
		super(context);
	}


	/**
	 * 初始化根应用上下文，本方法由servlet容器(tomcat、jetty)调用。。
	 *
	 * Initialize the root web application context.
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		// 而后交给Spring的ContextLoader来初始化根上下文
		initWebApplicationContext(event.getServletContext());
	}


	/**
	 * Close the root web application context.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		closeWebApplicationContext(event.getServletContext());
		ContextCleanupListener.cleanupAttributes(event.getServletContext());
	}

}
