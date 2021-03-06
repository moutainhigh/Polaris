package com.polaris.container.servlet.listener;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.polaris.container.ServerManager;
import com.polaris.container.listener.ServerListener;
import com.polaris.container.listener.ServerListenerExtension;
import com.polaris.core.component.LifeCycle;
import com.polaris.core.util.SpringUtil;

public class WebsocketListerner implements ServerListenerExtension{

	@Override
	public ServerListener[] getServerListeners() {
		return new ServerListener[] {new Websocket()};
	}
	
	public static class Websocket implements  ServerListener {

		@Override
		public void started(LifeCycle event) {
			if (event instanceof ServerManager) {
				if (((ServerManager)event).getContext() == null) {
		    		return;
		    	}
				if ((((ServerManager)event).getContext()) instanceof ServletContext) {
					ServletContext servletContext = (ServletContext)(((ServerManager)event).getContext());
			    	ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute("javax.websocket.server.ServerContainer");
			    	WSEndpointExporter wsEndpointExporter = new WSEndpointExporter();
			    	wsEndpointExporter.registerEndpoints(serverContainer);
				}
			}
		}
		
		public static class WSEndpointExporter implements  ServerListener{
			private static Logger logger = LoggerFactory.getLogger(WSEndpointExporter.class);
			
			@javax.annotation.Nullable
			private static ServerContainer serverContainer;
			private static Set<Class<?>> endpointClassSet = new LinkedHashSet<>();
			private static Set<ServerEndpointConfig> endpointConfigSet = new LinkedHashSet<>();
	
			/**
			 * Actually register the endpoints. Called by {@link #afterSingletonsInstantiated()}.
			 */
			public void registerEndpoints(ServerContainer serverContainer) {
				WSEndpointExporter.serverContainer = serverContainer;
				Set<Class<?>> endpointClasses = new LinkedHashSet<>();


				ApplicationContext context = SpringUtil.getApplicationContext();
				if (context != null) {
					String[] endpointBeanNames = context.getBeanNamesForAnnotation(ServerEndpoint.class);
					for (String beanName : endpointBeanNames) {
						endpointClasses.add(context.getType(beanName));
					}
				}

				for (Class<?> endpointClass : endpointClasses) {
					registerEndpoint(endpointClass);
				}
				
				for (Class<?> endpointClass : endpointClassSet) {
					registerEndpoint(endpointClass);
				}
				endpointClassSet.clear();
				if (context != null) {
					Map<String, ServerEndpointConfig> endpointConfigMap = context.getBeansOfType(ServerEndpointConfig.class);
					for (ServerEndpointConfig endpointConfig : endpointConfigMap.values()) {
						registerEndpoint(endpointConfig);
					}
					for (ServerEndpointConfig endpointConfig : endpointConfigSet) {
						registerEndpoint(endpointConfig);
					}
					endpointConfigSet.clear();
				}
			}

			public static void registerEndpoint(Class<?> endpointClass) {
				if (serverContainer == null) {
					endpointClassSet.add(endpointClass);
					return;
				}
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Registering @ServerEndpoint class: " + endpointClass);
					}
					serverContainer.addEndpoint(endpointClass);
				}
				catch (DeploymentException ex) {
					throw new IllegalStateException("Failed to register @ServerEndpoint class: " + endpointClass, ex);
				}
			}

			public static void registerEndpoint(ServerEndpointConfig endpointConfig) {
				if (serverContainer == null) {
					endpointConfigSet.add(endpointConfig);
					return;
				}
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Registering ServerEndpointConfig: " + endpointConfig);
					}
					serverContainer.addEndpoint(endpointConfig);
				}
				catch (DeploymentException ex) {
					throw new IllegalStateException("Failed to register ServerEndpointConfig: " + endpointConfig, ex);
				}
			}	
		}
	}


}

