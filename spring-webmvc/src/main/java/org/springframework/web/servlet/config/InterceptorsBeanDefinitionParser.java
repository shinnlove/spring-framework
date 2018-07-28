/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.servlet.config;

import java.util.List;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;
import org.springframework.util.xml.DomUtils;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} that parses a
 * {@code interceptors} element to register a set of {@link MappedInterceptor} definitions.
 *
 * @author Keith Donald
 * @since 3.0
 */
class InterceptorsBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * 主要依赖于下面这个方法解析xml中配置的标签：
	 * DomUtils.getChildElementsByTagName(Element element, String... childEleNames);
	 *
	 * @param element the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
	 * @param context
	 * @return
	 */
	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext context) {
		context.pushContainingComponent(
				new CompositeComponentDefinition(element.getTagName(), context.extractSource(element)));

		RuntimeBeanReference pathMatcherRef = null;
		if (element.hasAttribute("path-matcher")) {
			pathMatcherRef = new RuntimeBeanReference(element.getAttribute("path-matcher"));
		}

		// 到这里之后，element就是`<mvc:interceptors>`了，要检索里边的`<bean>`(全量路径拦截bean)、`interceptor`(选择性路径拦截器)
		List<Element> interceptors = DomUtils.getChildElementsByTagName(element, "bean", "ref", "interceptor");

		/**
		 * 类似这样的拦截配置，会在`<mvc:interceptors>`标签下找到两个元素，一个元素是`bean`、一个元素是`interceptor`。
		 *
		 * <mvc:interceptors>
		 * 		<bean class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor"/>
		 * 		<mvc:interceptor>
		 * 			<mvc:mapping path="/**" />
		 * 			<mvc:exclude-mapping path="/admin/**" />
		 * 			<mvc:exclude-mapping path="/images/**" />
		 * 			<bean class="org.springframework.web.servlet.theme.ThemeChangeInterceptor"/>
		 * 		</mvc:interceptor>
		 * </mvc:interceptors>
		 *
		 */

		// 遍历这两个元素
		for (Element interceptor : interceptors) {
			// 在根节点`RootBeanDefinition`定义的时候就设置beanClassType为`MappedInterceptor.class`类型
			RootBeanDefinition mappedInterceptorDef = new RootBeanDefinition(MappedInterceptor.class);

			mappedInterceptorDef.setSource(context.extractSource(interceptor));
			mappedInterceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

			ManagedList<String> includePatterns = null;
			ManagedList<String> excludePatterns = null;
			Object interceptorBean;

			if ("interceptor".equals(interceptor.getLocalName())) {
				// 找到xsd的`interceptor`标签，说明还有定义包含路径和除外路径

				includePatterns = getIncludePatterns(interceptor, "mapping");
				excludePatterns = getIncludePatterns(interceptor, "exclude-mapping");

				// 找单个`<mvc:interceptor>`标签里的`bean`或`ref`，这里会得到`<bean class="org.springframework.web.servlet.theme.ThemeChangeInterceptor"/>`这个元素
				Element beanElem = DomUtils.getChildElementsByTagName(interceptor, "bean", "ref").get(0);

				// 从解析上下文中取出beanDefinition定义委托去解析`bean`的子级元素(如果这个`bean`有成员变量的子元素定义在xml中的话，如一个服务bean)
				interceptorBean = context.getDelegate().parsePropertySubElement(beanElem, null);
			}
			else {
				// 没有`interceptor`标签的，说明是`bean`类型全量路径拦截
				interceptorBean = context.getDelegate().parsePropertySubElement(interceptor, null);
			}

			// 设置拦截器构造函数为3个参数类型：包含路径、除外路径、拦截器本身bean
			mappedInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(0, includePatterns);
			mappedInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(1, excludePatterns);
			mappedInterceptorDef.getConstructorArgumentValues().addIndexedArgumentValue(2, interceptorBean);

			if (pathMatcherRef != null) {
				mappedInterceptorDef.getPropertyValues().add("pathMatcher", pathMatcherRef);
			}

			// 将拦截器注册到bean工厂里
			String beanName = context.getReaderContext().registerWithGeneratedName(mappedInterceptorDef);
			context.registerComponent(new BeanComponentDefinition(mappedInterceptorDef, beanName));
		}

		context.popAndRegisterContainingComponent();
		return null;
	}

	private ManagedList<String> getIncludePatterns(Element interceptor, String elementName) {
		List<Element> paths = DomUtils.getChildElementsByTagName(interceptor, elementName);
		ManagedList<String> patterns = new ManagedList<>(paths.size());
		for (Element path : paths) {
			patterns.add(path.getAttribute("path"));
		}
		return patterns;
	}

}
