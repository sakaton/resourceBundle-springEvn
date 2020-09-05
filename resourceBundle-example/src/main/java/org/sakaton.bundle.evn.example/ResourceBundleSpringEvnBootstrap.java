package org.sakaton.bundle.evn.example;

import org.sakaton.bundle.evn.SpringContextLocal;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author zhengshijun
 * @version created on 2020/9/5.
 */
public class ResourceBundleSpringEvnBootstrap {

	public static void main(String[] args) {

		ConfigurableApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringContextLocal.class);
		applicationContext.start();
		String str = LocaleMessageBundle.getMessage("en",null,"key");

		System.out.println(str);

	}
}
