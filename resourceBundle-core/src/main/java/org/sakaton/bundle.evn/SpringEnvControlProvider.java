package org.sakaton.bundle.evn;

import java.util.ResourceBundle;
import java.util.spi.ResourceBundleControlProvider;

/**
 * @author sakaton
 * @version created on 2020/9/4.
 */
public class SpringEnvControlProvider implements ResourceBundleControlProvider {
	@Override
	public ResourceBundle.Control getControl(String baseName) {
		return new SpringEnvServerControl(baseName);
	}
}
