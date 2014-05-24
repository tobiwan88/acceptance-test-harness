package plugins;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.SMBContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over_cifs.CifsGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over_cifs.CifsGlobalConfig.Site;
import org.jenkinsci.test.acceptance.plugins.publish_over_cifs.CifsPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Feature: Tests for CIFS plugin
 *
 * @author Tobias Meyer
 */
@Native("docker")
@WithPlugins("publish-over-cifs")
public class CIFSPublishPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;
    @Inject
    SlaveProvider slaves;
    /**
     * Helper method to create a temporary empty directory
     *
     * @return File Descriptor for empty Directory
     * @throws java.io.IOException
     */
    private static File createTempDirectory()
            throws IOException {
        File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }
        temp = new File(temp.getPath() + "d");

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        temp.deleteOnExit();
        return (temp);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer with FTP Server with the name
     *
     * @param servername Name to Access Instance
     * @param smb       Docker Instance of the Server
     */
    private void jenkinsCifsConfigure(String servername, SMBContainer smb) {
        jenkins.configure();
        Site s = new CifsGlobalConfig(jenkins).addSite();
        {
            s.name.set(servername);
            s.hostname.set(smb.ipBound(139));
            s.port.set(smb.port(139));
            s.username.set(smb.getUsername());
            s.password.set(smb.getPassword());
            s.share.set("/tmp");
        }
        jenkins.save();
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP host
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory myfolder/
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_resources() throws IOException, InterruptedException {
        SMBContainer smbd = docker.start(SMBContainer.class);
        Resource cp_file = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsCifsConfigure("asd", smbd);
        j.configure();
        {
            j.copyResource(cp_file);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        j.startBuild().shouldSucceed();
        smbd.cp("/tmp/odes.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cp_file.asText()));
    }
}
