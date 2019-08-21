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
		} catch (IOException e) {
			throw new BranchMetricsCalculatorException(e.getMessage());
		}
	}

	@Override
	public Integer getAverageMeanCommitWaitingTimeInSeconds(String branchName) {
		
		Git git = new Git(gitRepository);
		
		RevCommit[] commits = getCommitsFromBranch(git, branchName);

        int previousCommitTime = 0;
        int elapsedTime = 0;
        
        int currentCommitTime = commits[0].getCommitTime();
        
        for(int i = 1; i<commits.length; i++) {     	
    		previousCommitTime = commits[i].getCommitTime();
    		elapsedTime += (currentCommitTime - previousCommitTime);
        }
 
        int average = elapsedTime / (commits.length-1);

        git.close();
		
		return average;	
	}
	
	private RevCommit[] getCommitsFromBranch(Git git, String branchName) {
		
        Iterable<RevCommit> commitIter = null;
        
		try {
			commitIter = git.log().add(git.getRepository().resolve("refs/heads/"+branchName)).call();
		} catch (RevisionSyntaxException | GitAPIException | IOException e) {
			git.close();
			throw new BranchMetricsCalculatorException(e.getMessage());	
		}
		
		RevCommit[] commitArray = 
				StreamSupport.stream(commitIter.spliterator(), false)
				.toArray(RevCommit[]::new);
		
		return commitArray;
	}
	
}
