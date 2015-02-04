package some.other.stub;

import com.leftstache.switchblade.core.*;

import javax.inject.*;

/**
 * @author Joel Johnson
 */
@Component
@Singleton
public class TestBeanListener implements BeanListener {
	@Override
	public void postConstruct(Object bean) {
		System.out.println();
	}
}
