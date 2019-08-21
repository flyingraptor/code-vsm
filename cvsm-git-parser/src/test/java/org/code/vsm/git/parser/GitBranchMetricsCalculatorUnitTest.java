package org.code.vsm.git.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GitBranchMetricsCalculatorUnitTest {
	
	private static File gitTestDirectory;
	
	private static Repository gitRepository;
	
	@BeforeAll
	public static void initRepository() throws IOException, IllegalStateException, GitAPIException, InterruptedException {
			
    	gitTestDirectory = File.createTempFile("cvsm", ".test");  
    	if(!gitTestDirectory.delete()) {
            throw new IOException("Could not delete temporary file " + gitTestDirectory);
        }
    	
    	Git git = Git.init().setDirectory(gitTestDirectory).call();
    	gitRepository = git.getRepository();
    	git.close();
	}
	
	@Test
	void testAverageWaitingTimeForCommit_1HourBetweenTwoCommits() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		createTwoCommitsWith1hourDiff();
		
		//Execute Test
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("master");
		
		//Assert
		assertEquals(3600, averageTimeInSeconds); //3600 are the seconds for 1 hour
	}
	
	private void createTwoCommitsWith1hourDiff() throws IOException, NoFilepatternException, GitAPIException {
		
		Git git = new Git(gitRepository);
		
        File fileToChange = new File(git.getRepository().getDirectory().getParent(), "testfileInRepo.txt");
        if(!fileToChange.createNewFile()) {
        	git.close();
            throw new IOException("Could not create file " + fileToChange);
        }
		
        //Make the 1st Commit
        long commitTimeInMillis = 1355306400000L; //12/12/2012 12:00:00
        makeCommit(git, fileToChange, "testfileInRepo.txt", "Message - 1st Commit", "Content - 1st commit", commitTimeInMillis);
        
        //Make the 2nd Commit
        commitTimeInMillis = 1355310000000L; //12/12/2012 13:00:00
        makeCommit(git, fileToChange, "testfileInRepo.txt", "Message - 2nd Commit", "Content - 2nd commit", commitTimeInMillis);
        
        git.close();
	}
	
	private void makeCommit(Git git, File file, String filesPattern, 
			String commitMessage, String changeContent, long commitTimeMillis) throws IOException, NoFilepatternException, GitAPIException {
		
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(changeContent);     
        writer.close();
        
        git.add()
            .addFilepattern(filesPattern)
            .call();
        
        PersonIdent person = new PersonIdent("Tester", "test@email.org", new TestTimestamp(commitTimeMillis));
        
        git.commit()
            .setMessage(commitMessage)
            .setAuthor(person)
            .setCommitter(person)
            .call();
	}
	
	@AfterAll
	public static void destroyRepository() throws IOException {
        FileUtils.deleteDirectory(gitTestDirectory);
	}
}
