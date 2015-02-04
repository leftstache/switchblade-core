package com.leftstache.switchblade.core;

import some.other.stub.*;
import org.junit.*;

import java.io.*;

/**
 * @author Joel Johnson
 */
public class ApplicationTest {
	@Test
	public void testApplication() throws IOException {
		SwitchbladeApplication<TestApplication> run = SwitchbladeApplication.create(TestApplication.class);
		run.start();
		TestApplication application = run.getApplication();
		System.out.println();
	}
}
