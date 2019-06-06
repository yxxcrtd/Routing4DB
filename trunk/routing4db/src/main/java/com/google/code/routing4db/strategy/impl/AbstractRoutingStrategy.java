package com.google.code.routing4db.strategy.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

import com.google.code.routing4db.strategy.RoutingStrategy;

public abstract class AbstractRoutingStrategy implements RoutingStrategy{

	/**
	 * �ڵ�ı�ż�����Դkey��ӳ�䡣��Ŵ��㿪ʼ�����ε�����
	 * һ������£���Ӧ���key���ö��ŷָ�
	 * */
	protected Map<Integer,List<String>> dataSourceKeyMap;
	
	/**
	 * ��ִ��·�ɵķ����б� ����������ʽƥ��, ��֧��*
	 * */
	private List<String> excludeMethodPatterns;
	
	/**
	 * logger
	 * */
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	
	/**
	 * ���˷����б������б��е÷�����ִ��·��
	 * */
	public  void route(Object target, Method method, Object[] args) {
		boolean needRoute = true;
		if(excludeMethodPatterns != null){ //�ж�֪����Ҫִ��·��
			String methodName = method.getName();
			for(String pattern : excludeMethodPatterns){
				if(PatternMatchUtils.simpleMatch(pattern, methodName)){
					needRoute = false;
					break;
				}
			}
		}
		if(needRoute){
			this.executeRoute(target, method, args);
		}else{
			if(logger.isDebugEnabled()){
				logger.debug(method.getName() + " match excludeMethodPatterns for routing, no routing for this method");
			}
	   }
	}
	
	/**
	 * ������Ҫִ��·�ɵķ���ִ��·��
	 * */
	public abstract void executeRoute(Object target, Method method, Object[] args);
	
	
	
	/**
	 * ������Ҫ�ӿ��в�����·�ɵķ����б�, ��֧���⼸����ʽ  "xxx*", "*xxx" and "*xxx*"
	 * */
	public void setExcludeMethodPatterns(List<String> excludeMethodPatterns) {
        if(excludeMethodPatterns != null){
        	//spring's typical "xxx*", "*xxx" and "*xxx*" pattern styles.
    		//����֧�����ŵ�ƥ���ʽ
    		List<String> compiledPattern = new ArrayList<String>(excludeMethodPatterns.size());
    		for(String readMethodPattern : excludeMethodPatterns){
    			if(StringUtils.countOccurrencesOf(readMethodPattern, "*") > 2){
    				throw new IllegalArgumentException("excludeMethodPatterns only suppoer follows pattern style: \"xxx*\", \"*xxx\", \"*xxx*\" and \"xxx*yyy\"  must not be null");
    			}
    			int first = readMethodPattern.indexOf('*');
    			int last = readMethodPattern.lastIndexOf('*');
    			if(first >0 && last >0  && (first + 1) == last){
    				throw new IllegalArgumentException("excludeMethodPatterns only suppoer follows pattern style: \"xxx*\", \"*xxx\", \"*xxx*\" and \"xxx*yyy\"  must not be null");
    			}
    			String tmp = readMethodPattern.trim();
    			compiledPattern.add(tmp);
    		}
        }
		this.excludeMethodPatterns = excludeMethodPatterns;
	}

	/**
	 * ����ʵ������Դ��key��ӳ��
	 * */
	public void setDataSourceKeyMap(Map<Integer, String> dataSourceKeyMap) {
		if(dataSourceKeyMap == null){
			throw new IllegalArgumentException("dataSourceKeyMap arugment must not be null");
		}
		if(dataSourceKeyMap.size() <= 0){
			throw new IllegalArgumentException("dataSourceKeyMap size must be big than zero");
		}
		//check num
	    for(int i=0; i<dataSourceKeyMap.size(); i++){
	    	if(dataSourceKeyMap.get(i) == null){
	    		throw new IllegalArgumentException("dataSourceKeyMap key must be serial num start with zero, ends with dataSourceKeyMap.size()-1. such 0 -->ka  1--> kb 2-->kc ");
	    	}
	    }
	    
	    //�������ŷָ�
	    Map<Integer,List<String>> dataSourceKeyMapTarget = new HashMap<Integer,List<String>>();
	    for(Entry<Integer,String> entry : dataSourceKeyMap.entrySet()){
	    	Integer num = entry.getKey();
	    	String value = entry.getValue();
	    	String[] values = value.split(",");
	    	List<String> valueList = new ArrayList<String>(values.length);
	    	for(String vl : values){
	    		if(vl.trim().length() == 0){
	    			continue;
	    		}
	    		valueList.add(vl.trim());
	    	}
	    	if(valueList.size() == 0){
	    		throw new IllegalArgumentException("key " + num + " --> must have dataSources");
	    	}
	    	dataSourceKeyMapTarget.put(num, valueList);
	    }

		this.dataSourceKeyMap = dataSourceKeyMapTarget;
	}

}
