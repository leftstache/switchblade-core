package com.leftstache.switchblade.core;

/**
 * @author Joel Johnson
 */
public interface ApplicationListener {
	void started(SwitchbladeApplication<?> application) throws Exception;
	void ended(SwitchbladeApplication<?> application) throws Exception;
}
