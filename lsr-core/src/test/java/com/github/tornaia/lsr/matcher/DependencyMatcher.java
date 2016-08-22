package com.github.tornaia.lsr.matcher;

import org.apache.maven.model.Dependency;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

import static org.hamcrest.CoreMatchers.is;

public class DependencyMatcher extends TypeSafeDiagnosingMatcher<Dependency> {

    private Matcher<String> groupId = new IsAnything<>();

    private Matcher<String> artifactId = new IsAnything<>();

    private Matcher<String> version = new IsAnything<>();

    public DependencyMatcher groupId(String groupId) {
        this.groupId = is(groupId);
        return this;
    }

    public DependencyMatcher artifactId(String artifactId) {
        this.artifactId = is(artifactId);
        return this;
    }

    public DependencyMatcher version(String version) {
        this.version = is(version);
        return this;
    }

    @Override
    protected boolean matchesSafely(Dependency item, Description mismatchDescription) {
        return matches(groupId, item.getGroupId(), "groupId value: ", mismatchDescription) &&
                matches(artifactId, item.getArtifactId(), "artifactId value: ", mismatchDescription) &&
                matches(version, item.getVersion(), "version value: ", mismatchDescription);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(Dependency.class.getSimpleName())
                .appendText(", groupId: ").appendDescriptionOf(groupId)
                .appendText(", artifactId: ").appendDescriptionOf(artifactId)
                .appendText(", version: ").appendDescriptionOf(version);
    }

    protected <X> boolean matches(Matcher<? extends X> matcher, X value, String attribute, Description mismatchDescription) {
        if (!matcher.matches(value)) {
            mismatchDescription.appendText(" " + attribute + " ");
            matcher.describeMismatch(value, mismatchDescription);
            return false;
        } else {
            return true;
        }
    }
}