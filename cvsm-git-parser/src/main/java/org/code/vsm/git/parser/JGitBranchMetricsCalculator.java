package org.code.vsm.git.parser;

import java.io.File;
import java.io.IOException;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Integer getAverageMeanCommitWaitingTimeInSeconds(String branchName) {
		
		Git git = new Git(gitRepository);
		
		RevCommit[] commitArr = null;
		try {
			commitArr = getCommitsArrayFromBranch(git, branchName);
		} catch (RevisionSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        int previousCommitTime = 0;
        int elapsed = 0;
        
        int lastCommitTime = commitArr[0].getCommitTime();
        
        for(int i = 1; i<commitArr.length; i++) {     	
    		previousCommitTime = commitArr[i].getCommitTime();
    		elapsed += (lastCommitTime - previousCommitTime);
        }
 
        int average = elapsed / (commitArr.length-1);

        git.close();
		
		return average;	
	}
	
	private RevCommit[] getCommitsArrayFromBranch(Git git, String branchName) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		
        Iterable<RevCommit> commitIter = null;
        
		try {
			commitIter = git.log().add(git.getRepository().resolve("refs/heads/"+branchName)).call();
		} catch (RevisionSyntaxException | GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
			git.close();
		}
		
		RevCommit[] commitArr = 
				StreamSupport.stream(commitIter.spliterator(), false)
				.toArray(RevCommit[]::new);
		
		return commitArr;
	}
	
}
