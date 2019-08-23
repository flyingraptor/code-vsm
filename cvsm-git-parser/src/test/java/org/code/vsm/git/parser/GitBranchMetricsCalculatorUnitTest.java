package org.code.vsm.git.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GitBranchMetricsCalculatorUnitTest {
	
	private static File gitTestDirectory;
	
	private static Repository gitRepository;
	
	@BeforeEach
	public void initRepository() throws IOException, IllegalStateException, GitAPIException, InterruptedException {
			
    	gitTestDirectory = File.createTempFile("cvsm", ".test");  
    	if(!gitTestDirectory.delete()) {
            throw new IOException("Could not delete temporary file " + gitTestDirectory);
        }
    	
    	Git git = Git.init().setDirectory(gitTestDirectory).call();
    	gitRepository = git.getRepository();
    	git.close();
	}
	
	@Test
	void testWith0Commit() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		File file = createFileInRepo("testfileInRepo.txt", "master");
		createSingleCommit(file, "master");
		
		//Execute Test
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("master");
		
		//Assert
		assertEquals(0, averageTimeInSeconds); //Since it's 0 commits there is 0 average
	}
	
	@Test
	void testWith1Commit() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		File file = createFileInRepo("testfileInRepo.txt", "master");
		createSingleCommit(file, "master");
		
		//Execute Test
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("master");
		
		//Assert
		assertEquals(0, averageTimeInSeconds); //Since it's only 1 commit there is 0 average
	}
	
	@Test
	void testWith1HourBetween2Commits() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		File file = createFileInRepo("testfileInRepo.txt", "master");
		create2CommitsWith1hourDiff(file, "master");
		
		//Execute Test
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("master");
		
		//Assert
		assertEquals(3600, averageTimeInSeconds); //3600 are the seconds for 1 hour
	}
	
	@Test
	void testWith3CommitsSeparatedByDifferenthours() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		createFileInRepo("master","testfileInRepo.txt");
		create3CommitsWith1hourDiff("master");
		
		//Execute
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("master");
		
		//Assert
		assertEquals(9000, averageTimeInSeconds);
	}
	
	@Test
	void testWith2CommitsSeparatedBy10Years() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		create2CommitsWith10yearsDiff("master");
		
		//Execute
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("master");
		
		//Assert
		assertEquals(315532800, averageTimeInSeconds); //315532800 are 10 years in seconds
	}
	
	@Test
	void testReturnErrorWhenGiveNotExistingBranch() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		File file = createFileInRepo("testfileInRepo.txt", "master");
		create2CommitsWith1hourDiff(file, "master");
		
		//Init class
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		
		//Execute and Assert that an exception thrown
		Assertions.assertThrows(BranchMetricsCalculatorException.class, () -> 
		{
				branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("wrongrepo");
		});
	}
	
	@Test
	void testReturnErrorWhenGiveNotExistingBranchAndNoCommits() throws IOException, NoFilepatternException, GitAPIException {
		
		//Init class
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		
		//Execute and Assert that an exception thrown
		Assertions.assertThrows(BranchMetricsCalculatorException.class, () -> 
		{
				branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("wrongrepo");
		});
	}
	
	@Test
	void testReturnErrorWhenGiveExistingBranchAndNoFilesAndCommits() throws IOException, NoFilepatternException, GitAPIException {
		
		//Execute Test
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		
		//Execute and Assert that an exception thrown
		Assertions.assertThrows(BranchMetricsCalculatorException.class, () -> 
		{
				branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds("master");
		});
	}
	
	@Test
	void testReturnErrorWhenGiveNullBranch() throws IOException, NoFilepatternException, GitAPIException {
		
		//Init class
		BranchMetricsCalculator branchMetricsCalculator = new JGitBranchMetricsCalculator(gitRepository.getDirectory());
		
		//Execute and Assert that an exception thrown
		Assertions.assertThrows(BranchMetricsCalculatorException.class, () -> 
		{
			branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds(null);
		});
	}
	
	@Test
	void testReturnErrorWhenGiveNotExistingRepo() throws IOException, NoFilepatternException, GitAPIException {
				
		//Execute and Assert that an exception thrown
		Assertions.assertThrows(BranchMetricsCalculatorException.class, () -> 
		{
			new JGitBranchMetricsCalculator(new File("dummy"));
		});
	}
	
	@Test
	void testReturnErrorWhenGiveNullRepo() throws IOException, NoFilepatternException, GitAPIException {
				
		//Execute and Assert that an exception thrown
		Assertions.assertThrows(BranchMetricsCalculatorException.class, () -> 
		{
			new JGitBranchMetricsCalculator(null);
		});
	}
	
	@Ignore//TODO: WIP
	void testMergeNotCountAsCommit() throws IOException, NoFilepatternException, GitAPIException {
		
		//Setup
		File fileInMaster = createFileInRepo("dummy.txt", "master");
		createSingleCommit(fileInMaster, "master");
		createBranch("testBranch1");
		File fileInBranch = createFileInRepo("testfileInRepo.txt", "testBranch1");
		create2CommitsWith1hourDiff(fileInBranch, "testBranch1");
        mergeBranch("testBranch1", "master", MergeCommand.FastForwardMode.NO_FF, false, "Merge Changes To Master");     
	}
	
	private void createBranch(String branchName) throws RefAlreadyExistsException, RefNotFoundException, 
							InvalidRefNameException, GitAPIException {
		Git git = new Git(gitRepository);
        git.branchCreate().setName(branchName).call();
        git.close();
	}
	
	private void mergeBranch(String fromBranch, String toBranch, MergeCommand.FastForwardMode mode, boolean squash, String message) throws NoHeadException, ConcurrentRefUpdateException, 
							CheckoutConflictException, InvalidMergeHeadsException, WrongRepositoryStateException, NoMessageException, 
							GitAPIException, RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		Git git = new Git(gitRepository);
		git.checkout().setName(toBranch).call();
		ObjectId mergeBase = gitRepository.resolve(fromBranch);
		
		git.merge().
                include(mergeBase).
                setCommit(true).
                setFastForward(mode).
                //setSquash(squash).
                setMessage(message).
                call();
		
		git.close();
	}
	
	private void createSingleCommit(File fileToChange, String branchName) throws IOException, NoFilepatternException, GitAPIException {
		
		Git git = new Git(gitRepository);
		
		if(gitRepository.getBranch() != null && !branchName.equals(branchName)) {
			git.checkout().setName(branchName).call();
		}
		
        //Make the 1st Commit
        long commitTimeInMillis = 1355302800000L; //12/12/2012 11:00:00
        makeCommit(git, fileToChange, fileToChange.getName(), "Message Commit", "Content commit", commitTimeInMillis);
        
        git.close();
	}
	
	private void create2CommitsWith1hourDiff(File fileToChange, String branchName) throws IOException, NoFilepatternException, GitAPIException {
		
		Git git = new Git(gitRepository);
		if(gitRepository.getBranch() != null && !branchName.equals(branchName)) {
			git.checkout().setName(branchName).call();
		}
		
        //Make the 1st Commit
        long commitTimeInMillis = 1355306400000L; //12/12/2012 12:00:00
        makeCommit(git, fileToChange, fileToChange.getName(), "Message - 1st Commit", "Content - 1st commit", commitTimeInMillis);
        
        //Make the 2nd Commit
        commitTimeInMillis = 1355310000000L; //12/12/2012 13:00:00
        makeCommit(git, fileToChange, fileToChange.getName(), "Message - 2nd Commit", "Content - 2nd commit", commitTimeInMillis);
        
        git.close();
	}
	
	private void create3CommitsWith1hourDiff(String branchName) throws IOException, NoFilepatternException, GitAPIException {
		
		Git git = new Git(gitRepository);
		git.checkout().setName(branchName);
		
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
        
        //Make the 3d Commit
        commitTimeInMillis = 1355324400000L; //12/12/2012 17:00:00
        makeCommit(git, fileToChange, "testfileInRepo.txt", "Message - 2nd Commit", "Content - 3rd commit", commitTimeInMillis);
        
        git.close();
	}
	
	private File createFileInRepo(String fileName, String branchName) throws IOException, RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException {
		Git git = new Git(gitRepository);
		
		if(gitRepository.getBranch() != null && !branchName.equals(branchName)) {
			git.checkout().setName(branchName).call();
		}
		
        File file = new File(git.getRepository().getDirectory().getParent(), fileName);
        if(!file.createNewFile()) {
        	git.close();
            throw new IOException("Could not create file " + file);
        }
        git.close();
        
        return file;
	}
	
	private void create2CommitsWith10yearsDiff(String branchName) throws IOException, NoFilepatternException, GitAPIException {
		
		Git git = new Git(gitRepository);
		if(gitRepository.getBranch() != null && !branchName.equals(branchName)) {
			git.checkout().setName(branchName).call();
		}
		
        File fileToChange = new File(git.getRepository().getDirectory().getParent(), "testfileInRepo.txt");
        if(!fileToChange.createNewFile()) {
        	git.close();
            throw new IOException("Could not create file " + fileToChange);
        }
		
        //Make the 1st Commit
        long commitTimeInMillis = 1355306400000L; //12/12/2012 12:00:00
        makeCommit(git, fileToChange, "testfileInRepo.txt", "Message - 1st Commit", "Content - 1st commit", commitTimeInMillis);
        
        //Make the 2nd Commit
        commitTimeInMillis = 1670839200000L; //12/12/2022 12:00:00
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
	
	//@AfterEach
	public void destroyRepository() throws IOException {
        FileUtils.deleteDirectory(gitTestDirectory);
	}
}
