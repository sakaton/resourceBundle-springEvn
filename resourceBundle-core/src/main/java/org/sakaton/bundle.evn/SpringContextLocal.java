package org.sakaton.bundle.evn;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author zhengshijun
 * @version created on 2020/9/4.
 */
public class SpringContextLocal implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextLocal.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return SpringContextLocal.applicationContext;
	}

	public static ConfigurableEnvironment getEnvironment() {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			return ConfigurableApplicationContext.class.cast(applicationContext).getEnvironment();
		}
		return null;
	}
}
