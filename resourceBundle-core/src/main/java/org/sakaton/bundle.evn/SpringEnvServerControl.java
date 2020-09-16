package org.sakaton.bundle.evn;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
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

	private final static String REFRESH_EVENT_CLASS_NAME = "org.springframework.cloud.endpoint.event.RefreshEvent";

	private final String[] ADDRESS_LOCAL = {"bootstrapProperties", "NACOS"};

	private final Charset charset;

	private final String baseName;

	private final static Map<String, PropertySource<?>> CACHE_SOURCE = new HashMap<>(16);

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
			bundle = new PropertySourceBundle();
			PropertySource<?> propertySource = getPropertySource((PropertySourceBundle) bundle, resourceName);
			if (Objects.isNull(propertySource)) {
				log.warn("propertySource is null resourceName:{}", resourceName);
				return super.newBundle(baseName, locale, format, loader, reload);
			}
			log.info("resourceName :{} loading result :{}", resourceName, propertySource);
			((PropertySourceBundle) bundle).refresh(propertySource);
		}
		return bundle;
	}

	private PropertySource<?> getPropertySource(PropertySourceBundle propertySourceBundle, final String resourceName) {
		if (CACHE_SOURCE.containsKey(resourceName)) {
			return CACHE_SOURCE.get(resourceName);
		}
		ApplicationContext applicationContext = SpringContextLocal.getApplicationContext();
		if (Objects.isNull(applicationContext) || !(applicationContext instanceof ConfigurableApplicationContext)) {
			return null;
		}
		ConfigurableApplicationContext configurable = ConfigurableApplicationContext.class.cast(applicationContext);
		ConfigurableEnvironment configurableEnvironment = configurable.getEnvironment();
		configurable.addApplicationListener(event -> {
			if (event.getClass().getName().equals(REFRESH_EVENT_CLASS_NAME)) {
				PropertySource<?> propertySource = doLoad(resourceName, configurableEnvironment);
				propertySourceBundle.refresh(propertySource);
			}
		});
		return doLoad(resourceName, configurableEnvironment);
	}


	private PropertySource<?> doLoad(final String resourceName, ConfigurableEnvironment configurableEnvironment) {
		String[] paths = ArrayUtils.add(ADDRESS_LOCAL, resourceName);
		PropertySources propertySources = configurableEnvironment.getPropertySources();
		PropertySource<?> propertySource = null;
		log.info("PropertySource paths : {}", StringUtils.join(paths, ","));
		for (String name : paths) {
			if (Objects.nonNull(propertySource)) {
				if (propertySource instanceof CompositePropertySource) {
					Collection<PropertySource<?>> collection = CompositePropertySource.class.cast(propertySource).getPropertySources();
					PropertySource<?> result = null;
					for (PropertySource<?> property : collection) {
						if (StringUtils.startsWithIgnoreCase(property.getName(), name)) {
							result = property;
						}
						log.debug("foreach source name :{}", property.getName());
					}
					propertySource = result;
				}
			} else {
				propertySource = propertySources.get(name);
			}
		}
		if (Objects.nonNull(propertySource)) {
			CACHE_SOURCE.put(resourceName, propertySource);
		}
		return propertySource;
	}
}
