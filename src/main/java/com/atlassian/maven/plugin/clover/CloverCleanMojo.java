package com.atlassian.maven.plugin.clover;

import com.atlassian.maven.plugin.clover.internal.AbstractCloverMojo;
import com.atlassian.maven.plugin.clover.internal.ConfigUtil;
import com.cenqua.clover.CloverNames;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Delete;

import java.io.File;

/**
 * The clover2:clean goal should be run directly from the command line,
 * when you are developing using the clover test runner optimizer.
 *
 * This mojo ensures that the file required by Clover to optimize your test is not deleted between builds.
 * 
 * @goal clean
 * @phase initialize
 */
public class CloverCleanMojo extends AbstractCloverMojo {

    /**
     *
     * A flag to indicate not to run clover2:clean for this execution.
     *
     * If set to true, clean will be skipped will not be run.
     *
     * @parameter expression="${maven.clover.clean.skip}" default-value="false"
     */
    protected boolean skip;


    public void execute() throws MojoExecutionException {
        if (skip) {
            return;
        }

        final Project project = new Project();
        project.setBasedir(getProject().getBasedir().getPath());
        project.addBuildListener(new MvnLogBuildListener(getLog()));
        project.init();
        // delete just the snapshot and the target/clover directory
        final File snapshotFile = new ConfigUtil(this).resolveSnapshotFile(snapshot);
        removeFile(snapshotFile, project);
        removeDir(new File(this.cloverOutputDirectory), project);
    }

    private void removeDir(File dir, Project project) throws MojoExecutionException {

        if (!dir.exists() || dir.isFile()) {
            return;
        }
        getLog().debug("Deleting directory: " + dir.getAbsolutePath());
        Delete delete = createDeleteTaskFor(project);
        delete.setDir(dir);
        delete.execute();
        if (dir.exists()) {
            getLog().warn("clover2:clean could not delete directory: " + dir);
        }
    }

    private Delete createDeleteTaskFor(Project project) {
        Delete delete = new Delete();
        delete.setProject(project);
        delete.setIncludeEmptyDirs(true);
        delete.init();
        return delete;
    }


    private void removeFile(File file, Project project) throws MojoExecutionException {

        if (!file.exists() || file.isDirectory()) {
            return;
        }
        getLog().debug("Deleting file: " + file.getAbsolutePath());
        Delete delete = createDeleteTaskFor(project);
        delete.setFile(file);
        delete.execute();
        if (file.exists()) {
            getLog().warn("clover2:clean could not delete file: " + file);
        }
    }
}
