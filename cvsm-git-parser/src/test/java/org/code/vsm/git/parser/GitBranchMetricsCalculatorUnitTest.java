package org.code.vsm.git.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GitBranchMetricsCalculatorUnitTest {
	
	private static File gitTestDirectory;
	
	private static Git git;
	
	@BeforeAll
	public static void initRepository() throws IOException, IllegalStateException, GitAPIException, InterruptedException {
		
    	gitTestDirectory = File.createTempFile("cvsm", ".test");  
    	if(!gitTestDirectory.delete()) {
            throw new IOException("Could not delete temporary file " + gitTestDirectory);
        }
    	
    	git = Git.init().setDirectory(gitTestDirectory).call();

        File myFile = new File(git.getRepository().getDirectory().getParent(), "testfile");
        if(!myFile.createNewFile()) {
            throw new IOException("Could not create file " + myFile);
        }
        
        //Add file and commit
        String str = "This is it. Try to find way to make the prog work.\n";
        BufferedWriter writer = new BufferedWriter(new FileWriter(myFile));
        writer.write(str);
         
        writer.close();
        
        git.add()
            .addFilepattern("testfile")
            .call();
        
        git.commit()
            .setMessage("Added testfile in master")
            .call();
        
        //Wait for 1 sec before making a second commit
        Thread.sleep(5000);
        
        //Change that file
        str = "This is it. In master no more changes\n";
        BufferedWriter writer3 = new BufferedWriter(new FileWriter(myFile));
        writer3.write(str);
        writer3.close();
        
        git.add()
        .addFilepattern("testfile")
        .call();
         
        git.commit()
        .setMessage("Change testfile in master")
        .call();
	}
	
	@Test
	void testAverageWaitingTimeForCommit_BetweenTwoCommits() {
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitTestDirectory);
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTime("master");
		assertEquals(5, averageTimeInSeconds);
	}
	
	@AfterAll
	public static void destroyRepository() throws IOException {
        FileUtils.deleteDirectory(gitTestDirectory);
	}
	

}
