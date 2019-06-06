package com.google.code.routing4db.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.code.routing4db.proxy.RountingProxyFactory;
import com.google.code.routing4db.strategy.RoutingStrategy;

public class RoutingSpringFactoryBean<T> implements FactoryBean<T>, InitializingBean{

	/**
	 * ����Ľӿ�
	 * */
	private Class<T> targetInterface;
	
	/**
	 * �������
	 * */
	private Object targetObject;
	
	/**
	 * ·�ɹ���
	 * */
	private RoutingStrategy routingStrategy;
	
	
	/**
	 * ���ش������
	 * */
	public T getObject() throws Exception {
		return RountingProxyFactory.proxy(targetObject, targetInterface, routingStrategy);
	}

	public Class<?> getObjectType() {
		return  targetInterface;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setTargetInterface(Class<T> targetInterface) {
		this.targetInterface = targetInterface;
	}

	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	public void setRoutingStrategy(RoutingStrategy routingStrategy) {
		this.routingStrategy = routingStrategy;
	}

	public void afterPropertiesSet() throws Exception {
	  if(targetObject == null || targetInterface == null || routingStrategy == null){
		  throw new IllegalArgumentException("targetObject, targetInterface, routingStrategy must not be null");
	  }
	}
}
