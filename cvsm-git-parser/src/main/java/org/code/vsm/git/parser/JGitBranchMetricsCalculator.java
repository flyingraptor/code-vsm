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

public class JGitBranchMetricsCalculator implements BranchMetricsCalculator {
	
	private Repository gitRepository;
	
	public JGitBranchMetricsCalculator(File gitDirectory) {		
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			gitRepository = builder.setGitDir(gitDirectory)
					.readEnvironment()
			        .findGitDir()
			        .build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Integer getAverageMeanCommitWaitingTime(String branchName) {
		
		Git git = new Git(gitRepository);
		
		RevCommit[] commitArr = getCommitsArrayFromBranch(git, branchName);

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
	
	private RevCommit[] getCommitsArrayFromBranch(Git git, String branchName) {
		
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
