package org.code.vsm.git.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

class JGitBranchMetricsCalculator implements BranchMetricsCalculator {
	
	private static final int ZERO_COMMITS_AVERAGE = 0;
	
	private static final int ONE_COMMIT = 1;
	
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
		
		validateBranch(branchName);
		
		Git git = new Git(gitRepository);
		
		RevCommit[] commits = getCommitsFromBranch(git, branchName);
		
		//If true then it mean either 1 or 0 commits in the branch so return 0 as average.
		if(commits.length <= ONE_COMMIT) {
			return ZERO_COMMITS_AVERAGE;
		}

        int previousCommitTime = 0;
        int elapsedTime = 0;
        
        int currentCommitTime = commits[0].getCommitTime(); //Get the HEAD commit
        
        for(int i = 1; i<commits.length; i++) {     	
    		previousCommitTime = commits[i].getCommitTime();
    		elapsedTime = elapsedTime + (currentCommitTime - previousCommitTime);
    		currentCommitTime = previousCommitTime;
        }
     
        int average = elapsedTime / (commits.length-1); /* length-1 because we divide with the number of gaps 
											         	* between commits NOT with the commits number. 
											         	* For example for 2 commits there is 1 gap between them. 
											         	* For 3 commits 2 gaps, 1 between 1st and 2nd and 1 between 2nd and 3rd.
											         	*/

        git.close();
		
		return average;	
	}
	
	private RevCommit[] getCommitsFromBranch(Git git, String branchName) {
		
        Iterable<RevCommit> commitIter = null;
        
		try {
			commitIter = git.log().add(git.getRepository().resolve("refs/heads/"+branchName)).call();
		} catch (NullPointerException e) {
			return new RevCommit[0];
		} catch (RevisionSyntaxException | GitAPIException | IOException e) {
			git.close();
			throw new BranchMetricsCalculatorException(e.getMessage());	
		}
		
		RevCommit[] commitArray = 
				StreamSupport.stream(commitIter.spliterator(), false)
				.toArray(RevCommit[]::new);
		
		return commitArray;
	}
	
	private void validateBranch(String branch) {
		
		if(branch == null) {
			throw new BranchMetricsCalculatorException("Null branch parameter");	
		}
		
		try {
			Git git = new Git(gitRepository);
			List<Ref> refs = git.branchList().call();
			git.close();

			boolean branchFound = false;
			for (Ref ref : refs) {
                String branchInRepo = ref.getName().substring(ref.getName().lastIndexOf("/")+1, ref.getName().length());
                if(branchInRepo.equals(branch)) {
                	branchFound = true;
                }
            }
			
			if(branchFound==false && refs.size() > 0) {
				throw new BranchMetricsCalculatorException("Branch not found");
			}
			
			if(refs.size() < 1) {
				throw new BranchMetricsCalculatorException("No Branches Found");
			}
			
		} catch (GitAPIException e) {
			throw new BranchMetricsCalculatorException(e.getMessage());
		}
	}
	
}
