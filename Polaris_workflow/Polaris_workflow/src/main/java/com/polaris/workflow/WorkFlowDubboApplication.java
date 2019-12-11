package com.polaris.workflow;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.polaris.dubbo.supports.MainSupport;

@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
public class WorkFlowDubboApplication {
	public static void main(String[] args) throws Exception { 
		MainSupport.startDubboServer(args,new Class[]{WorkFlowDubboApplication.class});
    } 
}