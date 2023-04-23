package net.stickycode.plugin.shifty;

import static org.assertj.core.api.StrictAssertions.assertThat;

import java.util.Properties;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.version.Version;
import org.junit.Test;

public class ShiftyFetchMojoComponentTest {

  private final class VersionImplementation
      implements Version {

    private final String value;

    private VersionImplementation(String highestVersion) {
      this.value = highestVersion;
    }

    @Override
    public String toString() {
      return value;
    }

    @Override
    public int compareTo(Version o) {
      return value.compareTo(o.toString());
    }
  }

  @Test
  public void noMetadata() {
    checkLocalSnapshots("1.999-SNAPSHOT", "1.999-SNAPSHOT", new MetadataNotFoundException(null, null, "Nothing"));
    checkLocalSnapshots("2.999-SNAPSHOT", "2.999-SNAPSHOT", new MetadataNotFoundException(null, null, "Nothing"));
    checkLocalSnapshots("[1.999-SNAPSHOT]", "1.999-SNAPSHOT", new MetadataNotFoundException(null, null, "Nothing"));
    checkLocalSnapshots("[2.999-SNAPSHOT]", "2.999-SNAPSHOT", new MetadataNotFoundException(null, null, "Nothing"));
  }

  @Test
  public void snapshots() {
    checkSnapshots("1.999-SNAPSHOT", "1.999-SNAPSHOT", "1.999-SNAPSHOT");
    checkSnapshots("[1.999-SNAPSHOT]", "1.999-SNAPSHOT", "1.999-SNAPSHOT");
  }

  @Test(expected = RuntimeException.class)
  public void noResolvedValues() {
    checkNoSnapshots("1.999-SNAPSHOT", "ERROR ERROR");
  }

  @Test
  public void resolve() {
    checkNoSnapshots("1.2", "1.115", "1.115");
    checkNoSnapshots("2.9", "2.8", "2.8");
    checkNoSnapshots("5.6", "5.9", "5.9");
    checkNoSnapshots("5.6", "5.9", "5.6", "5.9");
    checkNoSnapshots("5.6", "6.10", "5.9", "5.6", "6.5", "6.10");
    checkNoSnapshots("[5.6]", "5.6", "5.9", "5.6", "6.5", "6.10");
  }

  @Test
  public void checkSetProperties() {
    ShiftyFetchMojo mojo = mojo(true, true, null, "1.2");
    mojo.findArtifact(new DefaultArtifact("com.example:example:[1,2)")).getArtifact();
    assertThat(mojo.getProjectProperties().get("example.version")).isEqualTo("1.2");
    assertThat(mojo.getProjectProperties().get("example.contractVersion")).isEqualTo("1");
  }

  @Test
  public void checkSetPropertiesWithSingleVersion() {
    ShiftyFetchMojo mojo = mojo(true, true, null, "1");
    mojo.findArtifact(new DefaultArtifact("com.example:example:[1,2)")).getArtifact();
    assertThat(mojo.getProjectProperties().get("example.version")).isEqualTo("1");
    assertThat(mojo.getProjectProperties().get("example.contractVersion")).isEqualTo("1");
  }

  private void checkSnapshots(String givenVersion, String expectation, String... resolvedVersions) {
    check(false, false, givenVersion, expectation, null, resolvedVersions);
  }

  private void checkLocalSnapshots(String givenVersion, String expectation, Exception exception, String... resolvedVersions) {
    check(true, true, givenVersion, expectation, exception, resolvedVersions);
  }

  private void checkNoSnapshots(String givenVersion, String expectation, String... resolvedVersions) {
    check(true, false, givenVersion, expectation, null, resolvedVersions);
  }

  private void check(boolean ignoreSnapshots, boolean assumingSnapshotsAreLocal, String givenVersion, String expectation,
      Exception exception,
      String... resolvedVersions) {
    ShiftyFetchMojo mojo = mojo(ignoreSnapshots, assumingSnapshotsAreLocal, exception, resolvedVersions);
    Artifact artifact = mojo.findArtifact(new DefaultArtifact("com.example:example:" + givenVersion)).getArtifact();
    assertThat(artifact.getVersion()).isEqualTo(expectation);
  }

  private ShiftyFetchMojo mojo(boolean ignoreSnapshots, boolean assumingSnapshotsAreLocal, Exception exception,
      String... resolvedVersions) {
    ShiftyFetchMojo mojo = new ShiftyFetchMojo() {
      Properties p = new Properties();

      @Override
      VersionRangeResult resolveRangeRequest(VersionRangeRequest request) {
        VersionRangeResult result = new VersionRangeResult(request);

        for (String resolvedVersion : resolvedVersions)
          result.addVersion(new VersionImplementation(resolvedVersion));

        if (exception != null)
          result.addException(exception);

        return result;
      }

      @Override
      Properties getProjectProperties() {
        return p;
      }

      @Override
      boolean assumeSnapshotsAreLocal() {
        return assumingSnapshotsAreLocal;
      }

      @Override
      boolean ignoreSnapshots() {
        return ignoreSnapshots;
      }
    };
    return mojo;
  }

}
