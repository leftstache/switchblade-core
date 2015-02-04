package some.other.stub;

import com.google.inject.Singleton;

import javax.inject.Inject;

/**
 * @author Joel Johnson
 */
@Singleton
public class TestApplication {
	@Inject
	TestInject testInject;
}
