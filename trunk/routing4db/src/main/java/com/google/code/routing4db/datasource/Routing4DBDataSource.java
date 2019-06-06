package com.google.code.routing4db.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.google.code.routing4db.holder.RoutingHolder;


/**
 * ��������Դ·��
 * */
public class Routing4DBDataSource extends AbstractRoutingDataSource {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	
	public Routing4DBDataSource(){
		this.setLenientFallback(false); // ����key�Ҳ�����Ӧ����Դʱ���������ݴ���ֱ���׳��Ҳ�����Ӧ����Դ�Ĵ��� ��ֹ·��ʱ������Դ���ò��ԣ�������Ĭ������Դ���µĴ���
	}
	

	@Override
	protected Object determineCurrentLookupKey() {
		String dataSourceKey = RoutingHolder.getCurrentDataSourceKey();
		if(logger.isDebugEnabled()){
		   if(dataSourceKey == null){
			   logger.debug("none routing key, choose defaultDataSource for current connection");
		   }else{
			   logger.debug("choose dataSource for current connection by routing key " +  dataSourceKey );
		   }
		}
		return dataSourceKey;
	}
}
