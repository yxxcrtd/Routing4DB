package com.google.code.routing4db.strategy;

import java.lang.reflect.Method;

/**
 * ����Դ·�ɲ���
 * */
public interface RoutingStrategy {
	/**
	 * ִ�д˲��ԣ�ѡ���Ӧ������Դ��������key���õ�RoutingHolder�У����δ���ã������Ĭ������Դ
	 * @param  target   �����DAO����
	 * @param  method   DAO������ִ�еķ���
	 * @param  args     ����ִ������Ĳ���
	 * */
	public void route(Object target, Method method, Object[] args);

}
