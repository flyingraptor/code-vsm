package org.code.vsm.git.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

/**
 * Hello world!
 *
 */
public class cvsm 
{
	
	private static File gitTestDirectory; 
	
    public static void main( String[] args ) throws IllegalStateException, IOException, GitAPIException
    {	
    	
    	//args[0] repo path e.g. /Users/nikosraptis/repos/code-vsm/.git
    	//args[1] git branch
    	
    	//"/Users/nikosraptis/repos/code-vsm/.git"
    	//average-commit-waiting-time-single-branch
    	
    	BranchMetricsCalculator branchMetricsCalculator 
    		= new JGitBranchMetricsCalculator(new File(args[0]));
		Integer averageTimeInSeconds = branchMetricsCalculator.getAverageMeanCommitWaitingTimeInSeconds(args[1]);
		
		System.out.println(averageTimeInSeconds);
    	
        //App.nucfield();
    }
    
    private static void nucfield() throws IOException, IllegalStateException, GitAPIException {
    	
    	gitTestDirectory = File.createTempFile("cvsm", ".test");  
    	if(!gitTestDirectory.delete()) {
            throw new IOException("Could not delete temporary file " + gitTestDirectory);
        }
    	
    	try (Git git = Git.init().setDirectory(gitTestDirectory).call()) {
    		
            System.out.println("Having repository: " + git.getRepository().getDirectory());
            
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
            
            //Create extra branch and check out
            git.branchCreate().setName("testbranch1").call();
            git.checkout().setName("testbranch1").call();
            List<Ref> call = git.branchList().call();
            for (Ref ref : call) {
                System.out.println("Branch-Created: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
            
            //Change file and commit to the new branch
            String str2 = "This is it. Try to find way to make the prog work. And I added more text\n";
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(myFile));
            writer2.write(str2);
            
            writer2.close();
            
            git.add()
            .addFilepattern("testfile")
            .call();
             
            git.commit()
            .setMessage("Change testfile in branch")
            .call();
            
            //Change again file and commit to the new branch
            String str4 = "This is it. Try to find way to make the prog work. And I added more text. And more text\n";
            BufferedWriter writer4 = new BufferedWriter(new FileWriter(myFile));
            writer4.write(str4);
            
            writer4.close();
            
            git.add()
            .addFilepattern("testfile")
            .call();
             
            git.commit()
            .setMessage("Change testfile in branch")
            .call();

            System.out.println("Changed file " + myFile + " to repository at " 
            + git.getRepository().getDirectory());
            
            //Change again file and commit to the new branch
            String str5 = "Completely changed again with more letters\n";
            BufferedWriter writer5 = new BufferedWriter(new FileWriter(myFile));
            writer5.write(str5);
            
            writer5.close();
            
            git.add()
            .addFilepattern("testfile")
            .call();
             
            git.commit()
            .setMessage("Change testfile in branch")
            .call();
            
            //Change again file and commit to the new branch
            String str6 = "Another small change\n";
            BufferedWriter writer6 = new BufferedWriter(new FileWriter(myFile));
            writer6.write(str6);
            
            writer6.close();
            
            git.add()
            .addFilepattern("testfile")
            .call();
             
            git.commit()
            .setMessage("Change testfile in branch")
            .call();

            System.out.println("Changed file " + myFile + " to repository at " 
            + git.getRepository().getDirectory());
            
            //Checkout again to master
            git.checkout().setName("master").call();
            
            String str3 = "This is it. In master no more changes\n";
            BufferedWriter writer3 = new BufferedWriter(new FileWriter(myFile));
            writer3.write(str3);
            writer3.close();
            
            git.add()
            .addFilepattern("testfile")
            .call();
             
            git.commit()
            .setMessage("Change testfile in master")
            .call();

            System.out.println("Changed file " + myFile + " to repository at " 
            + git.getRepository().getDirectory());
            
            //Print all commits
        	Iterable<RevCommit> commits = git.log().all().call();
            int count = 0;
            for (RevCommit commit : commits) {
                System.out.println("Commit: " + commit  +
                		", name: " + commit.getName() + 
                		", id: " + commit.getId().getName() +
                		"Time: " + commit.getCommitTime());
                count++;
            }
            System.out.println("Count all commits: "+count);
            
            //Print commits in master
            count = 0;
            String treeName = "refs/heads/master";
            for (RevCommit commit : git.log().add(git.getRepository().resolve(treeName)).call()) {
                System.out.println(commit.getName());
                count++;
            }
            System.out.println("Commits in master: "+count);
            
            //Print commits in branch
            count = 0;
            
            Iterable<RevCommit> commitIter = git.log().not(git.getRepository().resolve("refs/heads/master"))
            		.add(git.getRepository().resolve("refs/heads/testbranch1")).call();
            
//            for (RevCommit commit : commitIter) {
//                System.out.println(commit.getName());
//                count++;
//            }
//            
//            System.out.println("Commits in branch: "+count);
            
            //Show Diffs in branch
            RevCommit[] commitArr = StreamSupport.stream(commitIter.spliterator(), false).toArray(RevCommit[]::new);
            
            for(int i=0; i<commitArr.length; i++) {
            	
            	System.out.println("***********Commit Time: " + commitArr[i].getCommitTime());
            	
            	if(i+1 == commitArr.length) {
            		break;
            	}
            	
                System.out.println("Old: " + commitArr[i+1].getName());
                System.out.println("New: " + commitArr[i].getName());
            	
        		try (ObjectReader reader = git.getRepository().newObjectReader()) {
            		CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            		oldTreeIter.reset(reader, commitArr[i+1].getTree());
            		CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            		newTreeIter.reset(reader, commitArr[i].getTree());

            		// finally get the list of changed files
            		try (Git git2 = new Git(git.getRepository())) {
                        List<DiffEntry> diffs= git2.diff()
                		                    .setNewTree(newTreeIter)
                		                    .setOldTree(oldTreeIter)
                		                    .call();
                        
                    	DiffFormatter formatter = new DiffFormatter(System.out);
                        for (DiffEntry entry : diffs) {
                            formatter.setRepository(git.getRepository());
                            formatter.format(entry);
                        }
                        formatter.close();
            		}
        		}
            }
            
           git.close(); 
           FileUtils.deleteDirectory(gitTestDirectory);
        }
        
    }
}
