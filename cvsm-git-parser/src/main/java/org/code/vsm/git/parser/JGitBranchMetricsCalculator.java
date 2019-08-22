package org.code.vsm.git.parser;

import java.io.File;
import java.io.IOException;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

class JGitBranchMetricsCalculator implements BranchMetricsCalculator {
	
	private Repository gitRepository;
	
	public JGitBranchMetricsCalculator(File repoDirectory) {		
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		try {
			repositoryBuilder.setMustExist(true);
			repositoryBuilder.setGitDir(repoDirectory);
		    gitRepository = repositoryBuilder.build();
		} catch (IOException | IllegalArgumentException | NullPointerException e) {
			throw new BranchMetricsCalculatorException(e.getMessage());
		}
	}

	@Override
	public Integer getAverageMeanCommitWaitingTimeInSeconds(String branchName) {
		
		Git git = new Git(gitRepository);
		
		RevCommit[] commits = getCommitsFromBranch(git, branchName);

        int previousCommitTime = 0; //Initialize
        int elapsedTime = 0; //Initialize
        
        int currentCommitTime = commits[0].getCommitTime(); //Get the HEAD commit
        
        for(int i = 1; i<commits.length; i++) {     	
    		previousCommitTime = commits[i].getCommitTime();
    		elapsedTime = elapsedTime + (currentCommitTime - previousCommitTime);
    		currentCommitTime = previousCommitTime;
        }
     
        /* 
         * length-1 because we divide with the number of gaps 
         * between commits NOT with the commits number. 
         * For example for 2 commits there is 1 gap between them. 
         * For 3 commits 2 gaps, 1 between 1st and 2nd and 1 between 2nd and 3rd.
         */
        int average = elapsedTime / (commits.length-1); 

        git.close();
		
		return average;	
	}
	
	private RevCommit[] getCommitsFromBranch(Git git, String branchName) {
		
        Iterable<RevCommit> commitIter = null;
        
		try {
			commitIter = git.log().add(git.getRepository().resolve("refs/heads/"+branchName)).call();
		} catch (RevisionSyntaxException | GitAPIException | IOException | NullPointerException e) {
			git.close();
			throw new BranchMetricsCalculatorException(e.getMessage());	
		}
		
		RevCommit[] commitArray = 
				StreamSupport.stream(commitIter.spliterator(), false)
				.toArray(RevCommit[]::new);
		
		return commitArray;
	}
	
}
