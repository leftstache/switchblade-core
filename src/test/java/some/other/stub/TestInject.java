package some.other.stub;

import com.google.inject.*;
import com.leftstache.switchblade.core.*;

/**
 * @author Joel Johnson
 */
@Component
@Singleton
public class TestInject {
	@Inject
	public TestInject2 testInject2;
}
