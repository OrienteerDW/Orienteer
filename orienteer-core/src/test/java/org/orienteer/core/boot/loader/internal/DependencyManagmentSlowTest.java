package org.orienteer.core.boot.loader.internal;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.core.boot.loader.internal.artifact.OArtifact;
import org.orienteer.core.boot.loader.internal.artifact.OArtifactReference;
import org.orienteer.junit.OrienteerTestRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(OrienteerTestRunner.class)
@Singleton
public class DependencyManagmentSlowTest {

	@Inject
    private InternalOModuleManager moduleManager;
	
    @Test
    public void resolveDependencies() {
        String groupId = "org.orienteer";
        String artifactId = "orienteer-birt";
        String version   = "2.0-SNAPSHOT";
        String gav = String.format("%s:%s:%s", groupId, artifactId, version);
        List<Artifact> resolvedDependency = moduleManager.resolveAndGetArtifactDependencies(new DefaultArtifact(gav));
        assertTrue("resolved dependencies can't be 0", resolvedDependency.size() > 0);
        for (Artifact artifact : resolvedDependency) {
            assertTrue("artifact don't contains file or file don't exists",
                    artifact.getFile() != null && artifact.getFile().exists());
        }
    }

    @Test
    public void downloadAndResolveOrienteerModules() throws IOException {
        List<OArtifact> modules = moduleManager.getOrienteerModules();
        for (OArtifact artifact : modules) {
            assertNotNull("Module from server can't be null", artifact);
        }
        for (OArtifact artifact : modules) {
            OArtifactReference reference = artifact.getArtifactReference();
            reference.setVersion(reference.getAvailableVersions().get(0));
            Artifact downloadedArtifact = moduleManager.downloadArtifact(reference.toAetherArtifact());
            assertNotNull("Can't resolve Orienteer module: " + artifact, downloadedArtifact);
            assertTrue("Downloaded modules jar file can't be null and must exists: " + downloadedArtifact,
                    downloadedArtifact.getFile() != null && downloadedArtifact.getFile().exists());
            List<Artifact> dependencies = moduleManager.resolveAndGetArtifactDependencies(downloadedArtifact);
            assertTrue("Dependencies of " + downloadedArtifact + " can't be empty", dependencies.size() > 0);
            for (Artifact dependency : dependencies) {
                assertTrue("Dependency jar file must exists", dependency.getFile() != null && dependency.getFile().exists());
            }
        }

    }

    @Test
    @Ignore // Unsafe for regular use
    public void deleteOArtifactFromLocalMavenRepository() throws Exception {
        String groupId    = "org.orienteer";
        String artifactId = "orienteer-birt";
        String version    = "1.3-SNAPSHOT";
        String gav = String.format("%s:%s:%s", groupId, artifactId, version);
        Artifact artifact = moduleManager.downloadArtifact(new DefaultArtifact(gav));
        assertNotNull("Downloaded artifact can't be null", artifact);
        assertNotNull("Jar file of downloaded artifact can't be null", artifact.getFile());
        OArtifactReference reference = OArtifactReference.valueOf(artifact);
        OArtifact oArtifact = new OArtifact(reference);
        assertTrue("Jar file of OArtifact must exist", oArtifact.getArtifactReference().getFile().exists());
        deleteOArtifact(oArtifact);
    }


    @Test
    @Ignore // Unsafe for regular use
    public void loadAndDeleteOrienteerModules() throws IOException {
        List<OArtifact> modules;
        //for (int i = 0; i < 100; i++) {
           modules = downloadAllOrienteerModulesFromServer();
           deleteOArtifacts(modules);
        //}
    }

    @Test
    public void testVersionRequest() {
        String groupId = "org.orienteer";
        String artifactId = "orienteer-devutils";
        List<String> versions = moduleManager.requestArtifactVersions(groupId, artifactId);
        assertTrue(versions != null && !versions.isEmpty());
        for (String version : versions) {
            assertTrue(!Strings.isNullOrEmpty(version));
        }
    }

    private List<OArtifact> downloadAllOrienteerModulesFromServer() throws IOException {
        List<OArtifact> modules = moduleManager.getOrienteerModules();
        List<OArtifact> result = Lists.newArrayList();
        for (OArtifact artifact : modules) {
            assertNotNull("Module from server can't be null", artifact);
            Artifact downloadedArtifact = moduleManager.downloadArtifact(artifact.getArtifactReference().toAetherArtifact());
            assertNotNull("Can't resolve Orienteer module: " + artifact, downloadedArtifact);
            assertTrue("Downloaded modules jar file can't be null and must exists: " + downloadedArtifact,
                    downloadedArtifact.getFile() != null && downloadedArtifact.getFile().exists());
            result.add(new OArtifact(OArtifactReference.valueOf(downloadedArtifact)));
        }
        return result;
    }

    private void deleteOArtifacts(List<OArtifact> oArtifacts) {
        for (OArtifact artifact : oArtifacts) {
            deleteOArtifact(artifact);
        }
    }

    private void deleteOArtifact(OArtifact oArtifact) {
        boolean deleted = moduleManager.deleteOArtifactFile(oArtifact);
        assertTrue("Jar file must deleted", deleted &&
                !oArtifact.getArtifactReference().getFile().exists());
    }
}