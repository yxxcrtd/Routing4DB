package com.google.code.routing4db.mybatis.example;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.google.code.routing4db.dao.User;
import com.google.code.routing4db.dao.UserDao;
import com.google.code.routing4db.holder.RoutingHolder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:write-master-read-slaves-mybatis.xml")
public class MybatisWriteMasterReadSlavesTest {
	@Resource
	UserDao userDao;
	
	@Resource
	UserDao userDaoTwo;
	
	@Resource
	JdbcTemplate jdbcTemplate;
	
	
	/**
	 * �������
	 * */
	@Test
	public void cleanData(){
		for(int i=0; i<10; i++){
			long id = i;
			RoutingHolder.setCurrentDataSourceKey(null); //Master
			jdbcTemplate.execute("delete from user where id = " + id);
		}
		for(int i= 10000;  i < 10008; i++){
			long id = i;
			RoutingHolder.setCurrentDataSourceKey(null); //Master
			jdbcTemplate.execute("delete from user where id = " + id);
		}
	}
	
	/**
	 * ���Զ�д����
	 * */
	@Test
	public void testWriteMasterReadSlaves(){
	   for(int i=1; i<10; i++){
		   User user = new User();
		   long id = i;
		   user.setId(id);
		   user.setName("User" + i);
		   //����master
		   userDao.insert(user);
		   
		   //����ɹ�
		   RoutingHolder.setCurrentDataSourceKey(null); //Master
		   int count =  jdbcTemplate.queryForInt("select count(*) from user where id = " + id);
		   Assert.assertEquals(count, 1);
		   System.out.println(user.getName());
		   
		   //��slave��
		   user = userDao.getUserById(id);
           Assert.assertNotNull(user);
		   System.out.println(user.getName());
	   }	
	}

	/**
	 * �������� ����mybatis�Ĵ�ͳʵ�ַ�ʽ֧������. ������mapper�ӿڣ���֧������
	 * */
	@Test
	public void testTransactionWithMybatisImpl(){
		for(int i= 10000;  i < 10008; i++){
			 long id = i;
			 User user = new User();
			 user.setId(id);
			 user.setName("User" + i);
			 try{
				 userDaoTwo.insertWithTransaction(user);
				 
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			//����master�� ������Ч������ʧ�� count Ϊ0
			 RoutingHolder.setCurrentDataSourceKey(null); //Master
			 int count =  jdbcTemplate.queryForInt("select count(*) from user where id = " + id);
			 Assert.assertEquals(0, count);
		}
	}
	
	
	/**
	 * �������� ����mybatis ��mapper��֧������
	 * */
	@Test
	public void testWithMapperNotSupportTransaction(){
		for(int i= 10000;  i < 10008; i++){
			 long id = i;
			 User user = new User();
			 user.setId(id);
			 user.setName("User" + i);
			 try{
				 //����master�� ������Ч������ʧ��
				 DefaultTransactionDefinition def = new DefaultTransactionDefinition();
				 def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
				 
				 userDao.insertWithTransaction(user);
				 
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			//����master�� ������Ч������ʧ�� count Ϊ0
			 RoutingHolder.setCurrentDataSourceKey(null); //Master
			 int count =  jdbcTemplate.queryForInt("select count(*) from user where id = " + id);
			 Assert.assertEquals(0, count);
		}
	}
	
	
}
