package net.stickycode.plugin.shifty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;

@Mojo(name = "fetch", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ShiftyFetchMojo
    extends AbstractMojo {

  /**
   * The current repository/network configuration of Maven.
   */
  @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
  private RepositorySystemSession session;

  @Parameter(required = true)
  private List<String> artifacts;

  @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
  private List<RemoteRepository> repositories;

  @Parameter(readonly = true, defaultValue = "${project}")
  private MavenProject project;

  @Component
  private RepositorySystem repository;

  @Parameter(defaultValue = "false")
  private Boolean includeSnapshots = false;

  @Parameter(defaultValue = "${project.build.directory}/shifty", required = true)
  private File outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    outputDirectory.mkdirs();

    log("resolving %s", artifacts);
    artifacts
      .parallelStream()
      .map(gav -> new DefaultArtifact(gav))
      .map(this::lookupArtifact)
      .forEach(this::copyArtifact);
  }

  Artifact lookupArtifact(DefaultArtifact artifact) {
    Version version = highestVersion(artifact);
    String propertyName = artifact.getArtifactId() + ".version";
    project.getProperties().setProperty(propertyName, version.toString());

    DefaultArtifact fetch = new DefaultArtifact(
      artifact.getGroupId(),
      artifact.getArtifactId(),
      artifact.getClassifier(),
      artifact.getExtension(),
      version.toString());
    ArtifactRequest request = new ArtifactRequest(fetch, repositories, null);
    return resolve(request).getArtifact();
  }

  void copyArtifact(Artifact artifact) {
    try {
      Files.copy(artifact.getFile().toPath(), new FileOutputStream(new File(outputDirectory, artifact.getFile().getName())));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ArtifactResult resolve(ArtifactRequest request) {
    try {
      return repository.resolveArtifact(session, request);
    }
    catch (ArtifactResolutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Version highestVersion(Artifact artifact) {
    VersionRangeRequest request = new VersionRangeRequest(artifact, repositories, null);
    VersionRangeResult v = resolve(request);

    if (!includeSnapshots) {
      List<Version> filtered = new ArrayList<Version>();
      for (Version aVersion : v.getVersions()) {
        if (!aVersion.toString().endsWith("SNAPSHOT")) {
          filtered.add(aVersion);
        }
      }
      v.setVersions(filtered);
    }

    if (v.getHighestVersion() == null) {
      throw (v.getExceptions().isEmpty()) ? new RuntimeException("Failed to resolve " + artifact.toString())
        : new RuntimeException("Failed to resolve " + artifact.toString(), v.getExceptions().get(0));
    }

    return v.getHighestVersion();
  }

  private VersionRangeResult resolve(VersionRangeRequest request) {
    try {
      return repository.resolveVersionRange(session, request);
    }
    catch (VersionRangeResolutionException e) {
      throw new RuntimeException(e);
    }
  }

  private void log(String message, Object... parameters) {
    getLog().info(String.format(message, parameters));
  }

}
