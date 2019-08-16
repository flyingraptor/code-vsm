package org.code.vsm.git.parser;

import java.io.File;
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
    public static void main( String[] args ) throws IllegalStateException, IOException, GitAPIException
    {
        System.out.println( "Hello World!" );
        App.nucfield();
    }
    
    private static void nucfield() throws IOException, IllegalStateException, GitAPIException {
    	
//    	File localPath = File.createTempFile("TestGitRepository", "");
//    	
//    	Git git = Git.init().setDirectory(localPath).call();
//        System.out.println("Having repository: " + git.getRepository().getDirectory());
//        
//        Iterable<RevCommit> commits = git.log().all().call();
//        int count = 0;
//        for (RevCommit commit : commits) {
//            System.out.println("LogCommit: " + commit);
//            count++;
//        }
//        System.out.println(count);
//        
//        FileUtils.deleteDirectory(localPath);
        
    }
}
