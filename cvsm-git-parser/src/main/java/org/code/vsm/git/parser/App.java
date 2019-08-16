package org.code.vsm.git.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static File gitTestDirectory; 
	
    public static void main( String[] args ) throws IllegalStateException, IOException, GitAPIException
    {
        System.out.println( "Hello World!" );
        App.nucfield();
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
            
            String str = "This is it. Try to find way to make the prog work.\n";
            BufferedWriter writer = new BufferedWriter(new FileWriter(myFile));
            writer.write(str);
             
            writer.close();
            
            git.add()
	            .addFilepattern("testfile")
	            .call();
            
            git.commit()
	            .setMessage("Added testfile")
	            .call();
            
            String str2 = "This is it. Try to find way to make the prog work. And I added more text\n";
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(myFile));
            writer2.write(str2);
             
            git.commit()
            .setMessage("Added testfile")
            .call();
            
            writer2.close();

            System.out.println("Added file " + myFile + " to repository at " 
            + git.getRepository().getDirectory());
            
        	Iterable<RevCommit> commits = git.log().all().call();
            int count = 0;
            for (RevCommit commit : commits) {
                System.out.println("LogCommit: " + commit);
                count++;
            }
            System.out.println(count);

            
            //FileUtils.deleteDirectory(gitTestDirectory);
        }
        
    }
}
