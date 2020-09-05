package org.sakaton.bundle.evn;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author sakaton
 * @version created on 2020/9/4.
 */
public class SpringEnvServerControl extends ResourceBundle.Control {

	private final static Logger log = LoggerFactory.getLogger(SpringEnvServerControl.class);

	/**
	 *  注意此处
	 *  默认获取的是 bootstrap.yml 配置的NACOS
	 */
	private final String[] ADDRESS_LOCAL = {"bootstrapProperties", "NACOS"};

	private final static String DEFAULT_GROUP_NAME = "DEFAULT_GROUP";

	private final Charset charset;

	private final String baseName;

	private final static Map<String, PropertySource<?>> CACHE_SOURCE = new HashMap<>();

	public SpringEnvServerControl(Charset charset, String baseName) {
		this.charset = charset;
		this.baseName = baseName;
	}

	public SpringEnvServerControl(String baseName) {
		this.baseName = baseName;
		this.charset = StandardCharsets.UTF_8;
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {

		String bundleName = toBundleName(baseName, locale);
		ResourceBundle bundle = null;
		if ("java.class".equals(format)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ResourceBundle> bundleClass
						= (Class<? extends ResourceBundle>) loader.loadClass(bundleName);

				// If the class isn't a ResourceBundle subclass, throw a
				// ClassCastException.
				if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
					bundle = bundleClass.newInstance();
				} else {
					throw new ClassCastException(bundleClass.getName()
							+ " cannot be cast to ResourceBundle");
				}
			} catch (ClassNotFoundException ignored) {
			}
		} else if ("java.properties".equals(format)) {
			String resourceName = toResourceName(bundleName, "properties");
			resourceName = new File(resourceName).getName();
			PropertySource<?> propertySource = getPropertySource(resourceName);
			if (Objects.isNull(propertySource)) {
				log.warn("propertySource is null resourceName:{}", resourceName);
				return super.newBundle(baseName, locale, format, loader, reload);
			}
			log.info("resourceName :{} loading result :{}", resourceName, propertySource);
			bundle = new PropertySourceBundle(propertySource);
		}
		return bundle;
	}

	private PropertySource<?> getPropertySource(String resourceName) {
		if (CACHE_SOURCE.containsKey(resourceName)) {
			return CACHE_SOURCE.get(resourceName);
		}
		ConfigurableEnvironment configurableEnvironment = SpringContextLocal.getEnvironment();
		if (Objects.isNull(configurableEnvironment)){
			return null;
		}
		// 注意此处 资源名称默认添加分组  messages_en.properties,DEFAULT_GROUP
		resourceName = resourceName.concat(",").concat(DEFAULT_GROUP_NAME);

		String[] paths = ArrayUtils.add(ADDRESS_LOCAL, resourceName);
		PropertySources propertySources = configurableEnvironment.getPropertySources();
		PropertySource<?> propertySource = null;
		for (String name : paths) {
			if (Objects.nonNull(propertySource)) {
				if (propertySource instanceof CompositePropertySource) {
					Collection<PropertySource<?>> collection = CompositePropertySource.class.cast(propertySource).getPropertySources();
					PropertySource<?> result = null;
					for (PropertySource<?> property : collection) {
						if (StringUtils.equalsIgnoreCase(property.getName(), name)) {
							result = property;
						}
					}
					propertySource = result;
				}
			} else {
				propertySource = propertySources.get(name);
			}
		}
		if (Objects.nonNull(propertySource)){
			CACHE_SOURCE.put(resourceName,propertySource);
		}
		return propertySource;
	}
}
