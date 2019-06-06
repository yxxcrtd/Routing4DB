package com.google.code.routing4db.proxy;

import java.lang.reflect.Proxy;

import com.google.code.routing4db.strategy.RoutingStrategy;

/**
 * ����ʵ�ʶ���Ĵ������ش������
 * */
public abstract class RountingProxyFactory {
	
    /***
     * ���ض����·�ɴ������
     * */
	@SuppressWarnings("unchecked")
	public static <T> T proxy(Object target, Class<T> interfaceClass, RoutingStrategy routingStrategy) {
		if(target == null || interfaceClass == null || routingStrategy == null){
			throw new IllegalArgumentException("arugments(target, interfaceClass, routingStrategy) must not be null");
		}
		RoutingInvocationHanlder handler = new RoutingInvocationHanlder(target, interfaceClass, routingStrategy);
		return (T) Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{interfaceClass}, handler);
	}
	
}
