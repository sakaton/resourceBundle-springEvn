package org.sakaton.bundle.evn;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.PropertySource;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sakaton
 * @version created on 2020/9/5.
 */
public class PropertySourceBundle extends ResourceBundle {

	private Map<String, Object> source = new ConcurrentHashMap<>(128);

	private PropertySource<?> original;

	public PropertySourceBundle(PropertySource<?> propertySource) {
		refresh(propertySource);
	}
	public PropertySourceBundle(){

	}

	@Override
	protected Object handleGetObject(String key) {
		key = StringUtils.upperCase(StringUtils.trim(key));
		return Objects.nonNull(source) ? source.get(key) : null;
	}

	@Override
	public Enumeration<String> getKeys() {
		return toEnumerate(source.keySet());
	}

	@SuppressWarnings("unchecked")
	public void refresh(PropertySource<?> propertySource){
		if (original == propertySource){
			return;
		}
		original = propertySource;
		if (Objects.nonNull(original) && original.getSource() instanceof Map) {
			Map<String, Object> source = (Map<String, Object>) original.getSource();
			this.source.clear();
			source.forEach((key, value) -> this.source.put(StringUtils.upperCase(StringUtils.trim(key)), value));
		} else {
			source = null;
		}
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
