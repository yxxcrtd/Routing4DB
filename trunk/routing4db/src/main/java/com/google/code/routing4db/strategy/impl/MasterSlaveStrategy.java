package com.google.code.routing4db.strategy.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.PatternMatchUtils;

import com.google.code.routing4db.holder.RoutingHolder;


/**
 * ʵ��Master-Slave·�ɲ��ԡ� ����Master��Ĭ������Դ�� Slave����Ĭ������Դָ����
 * дMaster�� ��Slave�� ��ͨ����Master��key�ŵ�Slave��ʵ��дMaster����Master-Slave
 * ���Slave���������ѡ��һ����
 * */
public class MasterSlaveStrategy  extends AbstractRoutingStrategy implements  InitializingBean{
	
	/**
	 * ���ķ����б� ����������ʽƥ��, ��֧��*
	 * */
	private List<String> readMethodPatterns;
	
	/**
	 * Master����Դ��key�� δ���������Ĭ������Դ��Ϊmaster������Դ
	 * */
	private String masterDataSourceKey;
	
	
	
	/**
	 * �����, ���ѡ��һ��Slave
	 * */
	private Random random;
	
	
	public MasterSlaveStrategy(){
		random = new Random();
	}

	/**
	 * ִ��Master-Slave·�ɲ��ԡ������д����ѡ��master����Դ�����򣬴����õ�����Դ�����ѡ��һ�����ж���
	 * */
	public void executeRoute(Object target, Method method, Object[] args) {
		boolean isReadMethod = false;
		String methodName = method.getName();
		for(String pattern : readMethodPatterns){
			if(PatternMatchUtils.simpleMatch(pattern, methodName)){
				isReadMethod = true;
				break;
			}
		}
		if(!isReadMethod){
			if(logger.isDebugEnabled()){
				logger.debug("method: " +  methodName + " --> routing to master datasource: " + masterDataSourceKey);
			}
			RoutingHolder.setCurrentDataSourceKey(masterDataSourceKey);
			return;
		}
		int mapKey = random.nextInt(dataSourceKeyMap.size());
		List<String> keys = dataSourceKeyMap.get(mapKey);
		int index = random.nextInt(keys.size());
		String slaveDataSourceKey = keys.get(index);
		if(logger.isDebugEnabled()){
		    logger.debug("method: " +  methodName + " --> routing to slave datasource: " + slaveDataSourceKey);
		}
		RoutingHolder.setCurrentDataSourceKey(slaveDataSourceKey);
	}


	public void setReadMethodPatterns(List<String> readMethodPatterns) {
		this.readMethodPatterns = ValidateUtils.validReadMethodPatterns(readMethodPatterns);
	}

	public void setMasterDataSourceKey(String masterDataSourceKey) {
		this.masterDataSourceKey = masterDataSourceKey;
	}

	public void afterPropertiesSet() throws Exception {
		if(readMethodPatterns == null || dataSourceKeyMap == null){
			throw new IllegalArgumentException("readMethodPatterns and slaveDataSourceKeyMap arugment must not be null");
		}
	}

}
