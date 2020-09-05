package org.sakaton.bundle.evn;

import org.springframework.core.env.PropertySource;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author sakaton
 * @version created on 2020/9/5.
 */
public class PropertySourceBundle extends ResourceBundle {

	private final PropertySource<?> propertySource;

	public PropertySourceBundle(PropertySource<?> propertySource) {
		this.propertySource = propertySource;
	}

	@Override
	protected Object handleGetObject(String key) {
		return propertySource.getProperty(key);
	}


	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getKeys() {
		Object source = propertySource.getSource();
		Map<String, String> map = (Map<String, String>) source;
		return toEnumerate(map.keySet());
	}

	/**
	 * 转换
	 *
	 * @param dataList list数组
	 * @param <T>      泛型
	 * @return 转换之后
	 */
	public static <T> Enumeration<T> toEnumerate(Collection<T> dataList) {
		Iterator<T> iterator = Objects.nonNull(dataList) ? dataList.iterator() : null;
		return new Enumeration<T>() {
			@Override
			public boolean hasMoreElements() {
				return Objects.nonNull(iterator) ? iterator.hasNext() : Boolean.FALSE;
			}

			@Override
			public T nextElement() {
				return iterator.next();
			}
		};
	}
}
