import org.apache.commons.pool.PoolUtils;

public class ClassWithTheMovingDependency {
	
	public static void method() {
		new PoolUtils();
	}
}
