package com.leftstache.switchblade.core;

/**
 * @author Joel Johnson
 */
public interface BeanListener {
	void postConstruct(Object bean) throws Exception;
}
