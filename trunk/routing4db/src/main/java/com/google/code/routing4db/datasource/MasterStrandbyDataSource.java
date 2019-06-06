package com.google.code.routing4db.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

public class MasterStrandbyDataSource extends AbstractDataSource implements InitializingBean{

	/**
	 * ����ͱ�������Դ��
	 * */
	private Object masterDataSource;
	
	private Object standbyDataSource;
	
	
	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
	
	/**
	 * ������ı�׼����Դ
	 * */
	private DataSource resolvedMasterDataSource;
	
	private DataSource resolvedStandbyDataSource;

	/**
	 * ��ǰ��������
	 * */
	protected DataSource currentDataSource;
	

	/**
	 * ���������ļ���������������ͨ�������Խ������ã���checkTimeInterval��checkAvailableSql
	 * */
	private Properties configProperties;
	
	
	/**���������ֶΣ� ��ʱ�����⹫�����������configProperties ��������*/
	/**
	 * ���ʱ����, ��λms Ĭ��10��
	 * */
	private long checkTimeInterval = 10000;
	
	/**
	 * �������Դ�Ƿ���õ����,Ĭ����select 1 �� oracle�����޸�Ϊ select 1 from dual 
	 * */
	private String checkAvailableSql = "select 1";
	
	
	/**
	 * ������ԭ�Ӽ���������������Դ�л���ȷ�����ҽ���һ���߳�ִ������Դ�л�
	 * */
	private AtomicInteger lock = new AtomicInteger(0);
	
	
	/**
	 * ��д��ȡ���ӵķ������ڵ�ǰ����Դ������ʱ����������Դ�л�
	 * */
	@Override
	public Connection getConnection() throws SQLException {
		DataSource sessionDataSource = this.getCurrentDataSource(); //���λ�ȡ���ӵ�����Դ
		try{
			return sessionDataSource.getConnection();
		}catch(SQLException sqle){
			 logger.error("Get Connection Exception " + currentDataSource , sqle);
			 if(sessionDataSource == this.getCurrentDataSource()){ //���̻߳����п����Ѿ��л�
				    this.switchToAvailableDataSource(); //�Զ��л�
			 }
			 throw sqle;
		}
	}
	
	/**
	 * ��д��ȡ���ӵķ������ڵ�ǰ����Դ������ʱ����������Դ�л�
	 * */
	@Override
	public Connection getConnection(String username, String password)throws SQLException {
		DataSource sessionDataSource = this.getCurrentDataSource(); //���λ�ȡ���ӵ�����Դ
		try{
		  return sessionDataSource.getConnection(username, password);
		}catch(SQLException sqle){
			 logger.error("Get Connection With Args Exception " + currentDataSource , sqle);
			 if(sessionDataSource == this.getCurrentDataSource()){ //���̻߳����п����Ѿ��л�
			        this.switchToAvailableDataSource(); 
			 }
			 throw sqle;
		}
	}

		
	/**
	 * �������Դ��Ч�ԣ����ݲ��Խ����������л����������£�
	 * 1�������ǰ���ӵ��Ǳ��⣬��������Ƿ���ã�������ã��л������⡣
	 * 2�������ǰ���������⣬��������Ƿ���ã���������ã��л�������
	 * */
	protected void switchToAvailableDataSource(){
		try{
			if(lock.incrementAndGet() > 1){ //������һ���߳�ȥ�������Դ�Ƿ���Ч���������л�
				return;
			}
			
			if(currentDataSource == resolvedStandbyDataSource){
				if(this.isDataSourceAvailable(resolvedMasterDataSource)){
					currentDataSource = resolvedMasterDataSource;
				}
			}else{
				currentDataSource = resolvedMasterDataSource;
				if(!this.isDataSourceAvailable(resolvedMasterDataSource)){
					currentDataSource =  resolvedStandbyDataSource;
				}
			}
		}finally{
			lock.decrementAndGet();
		}
	}
	
	
	
	/**
	 * ��������Ƿ����, ����Ե������쳣����׼���쳣
	 * */
	protected boolean isDataSourceAvailable(DataSource dataSource){
		Connection  conn = null;
		try{
			 conn = dataSource.getConnection();
			 Statement stmt = conn.createStatement();
			 boolean success = stmt.execute(checkAvailableSql); //���ִ�гɹ����᷵�ؽ��
			 stmt.close();
			 return success;
		}catch(SQLException e){
			logger.error("CheckDataSourceAvailable Exception", e);
			return false;
		}finally{
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Close Connection Exception", e);
				}
			}
		}
	}

	/**
	 * Resolve the specified data source object into a DataSource instance.
	 * <p>The default implementation handles DataSource instances and data source
	 * names (to be resolved via a {@link #setDataSourceLookup DataSourceLookup}).
	 * @param dataSource the data source value object as specified in the
	 * {@link #setTargetDataSources targetDataSources} map
	 * @return the resolved DataSource (never <code>null</code>)
	 * @throws IllegalArgumentException in case of an unsupported value type
	 */
	protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		}
		else if (dataSource instanceof String) {
			return this.dataSourceLookup.getDataSource((String) dataSource);
		}
		else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
		}
	}
	
	/**
	 * Set the DataSourceLookup implementation to use for resolving data source
	 * name Strings in the {@link #setTargetDataSources targetDataSources} map.
	 * <p>Default is a {@link JndiDataSourceLookup}, allowing the JNDI names
	 * of application server DataSources to be specified directly.
	 */
	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}
	
	protected DataSource getCurrentDataSource(){
		return currentDataSource;
	}
	
	public void setMasterDataSource(Object masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public void setStandbyDataSource(Object standbyDataSource) {
		this.standbyDataSource = standbyDataSource;
	}
	

	public void setConfigProperties(Properties configProperties) {
		this.configProperties = configProperties;
	}
	

	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.masterDataSource == null) {
			throw new IllegalArgumentException("Property 'masterDataSource' is required");
		}
		if(this.standbyDataSource == null){
			throw new IllegalArgumentException("Property 'standbyDataSource' is required");
		}
		
		if(configProperties != null){
			String checkTimeIntervalStr = configProperties.getProperty("checkTimeInterval");
			if(checkTimeIntervalStr == null){
				logger.info("configProperties --> checkTimeInterval property not config, use default " + checkTimeInterval);
			}else{
				checkTimeInterval = Long.parseLong(checkTimeIntervalStr);
				logger.info("configProperties --> checkTimeInterval property config value " + checkTimeInterval);
			}
			
			if(checkTimeInterval <= 0){ //���ʱ����
				throw new IllegalArgumentException("Property checkTimeInterval must above zero");
			}
			
			String checkAvailableSqlStr = configProperties.getProperty("checkAvailableSql");
			if(checkAvailableSqlStr == null){
				logger.debug("configProperties --> checkAvailableSql property not config, use default sql( select 1). if you use oracle please config it with a right sql sucn as select 1 from dual");
			}else{
				checkAvailableSql =  checkAvailableSqlStr;
				logger.info("configProperties --> checkAvailableSql config sql is " + checkAvailableSql);
			}
		}else{
			logger.info("configProperties not configed, use default config");
			logger.info("configProperties --> checkAvailableSql property not config, use default " + checkAvailableSql);
			logger.info("configProperties --> checkTimeInterval property not config, use default " + checkTimeInterval);
		}
		
		//�����������Դ
		resolvedMasterDataSource = this.resolveSpecifiedDataSource(masterDataSource);
		resolvedStandbyDataSource = this.resolveSpecifiedDataSource(standbyDataSource);
		currentDataSource  = this.resolvedMasterDataSource;
		//����Daemon�߳�
		Thread thread = new CheckMasterAvailableDaemonThread();
		thread.start();
	}




	/**
	 * ����̣߳��л�����������������ã����л�������
	 * */
	private class CheckMasterAvailableDaemonThread extends Thread{
		public CheckMasterAvailableDaemonThread(){
			this.setDaemon(true);
			this.setName("MasterStandbyCheckMasterAvailableDaemonThread");
		}
		 @Override
		 public void run() {
			 while(true){
				 switchToAvailableDataSource();
				 try {
					Thread.sleep(checkTimeInterval);
				} catch (InterruptedException e) {
					logger.warn("Check Master InterruptedException", e);
				}
			 }
		 }
	}
	
	
}
