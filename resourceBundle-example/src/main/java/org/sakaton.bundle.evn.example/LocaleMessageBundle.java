package org.sakaton.bundle.evn.example;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.spi.ResourceBundleControlProvider;

/**
 * @author zhengshijun
 * @version created on 2020/9/4.
 */
public class LocaleMessageBundle {

	private static final String FILE_BASE_NAME = "locale.messages";
	private static ResourceBundleControlProvider provider;
	static {

		ServiceLoader<ResourceBundleControlProvider> serviceLoaders
				= ServiceLoader.load(ResourceBundleControlProvider.class);
		Iterator<ResourceBundleControlProvider> iterator = serviceLoaders.iterator();
		if (iterator.hasNext()){
			provider = iterator.next();
		} else {
			provider = null;
		}
	}

	public static String getMessage(String language, String country, String key) {
		Locale locale;
		if (StringUtils.isNotBlank(country)) {
			locale = new Locale(language, country);
		} else {
			locale = new Locale(language);
		}
		String result;
		try {



			ResourceBundle resourceBundle;
			if (Objects.isNull(provider)){
				resourceBundle = ResourceBundle.getBundle(FILE_BASE_NAME, locale);
			} else {
				resourceBundle = ResourceBundle.getBundle(FILE_BASE_NAME, locale,provider.getControl(FILE_BASE_NAME));
			}
			result = resourceBundle.getString(key);
			result = StringUtils.isBlank(result) ? result : new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return null;
		}
		return result;
	}

	public static void main(String[] args) {
		System.out.println(getMessage("en",null,"report.employee.online.type.1"));

	}
}
