package net.stickycode.plugin.shifty;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class ShiftyFetchMojoTest {

  @Test
  public void sanity() throws MojoExecutionException, MojoFailureException {
    new ShiftyFetchMojo();
  }

}
