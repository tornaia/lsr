import org.apache.commons.lang3.StringUtils;

public class ClassWithTheMovingDependency {
	
	public static void method() {
		StringUtils.isEmpty("");
	}
}
