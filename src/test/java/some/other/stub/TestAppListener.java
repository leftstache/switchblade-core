package some.other.stub;

import com.leftstache.switchblade.core.*;

import javax.inject.*;

/**
 * @author Joel Johnson
 */
@Component
public class TestAppListener implements ApplicationListener {
	@Override
	public void started(SwitchbladeApplication<?> application) {
		application.close();
		System.out.println();
	}

	@Override
	public void ended(SwitchbladeApplication<?> application) {
		System.out.println();
	}
}
