package com.polaris.core.config.properties;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class ConfigurationGblImport implements ImportBeanDefinitionRegistrar{
	
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		String beanName = ConfigurationGbl.BEAN_NAME;
		if (!registry.containsBeanDefinition(beanName)) {
			GenericBeanDefinition definition = new GenericBeanDefinition();
			definition.setBeanClass(ConfigurationGbl.class);
			definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(beanName, definition);
		}
	}


}
