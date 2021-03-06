package com.polaris.config.nacos;

import org.springframework.core.annotation.Order;

import com.polaris.core.config.ConfHandlerListener;
import com.polaris.core.config.ConfHandlerOrder;
import com.polaris.core.config.ConfHandler;

@Order(ConfHandlerOrder.NACOS)
public class ConfNacosHandler implements ConfHandler {

	@Override
	public String get(String fileName, String group) {
		return ConfNacosClient.getInstance().getConfig(fileName,group);
	}

	@Override
	public void listen(String fileName, String group, ConfHandlerListener listener) {
		ConfNacosClient.getInstance().addListener(fileName, group, listener);
	}
}
