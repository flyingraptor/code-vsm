package org.code.vsm.git.parser;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.apache.commons.io.FileUtils;

@Tag("AcceptanceTest")
public class SingleBranchShowStatisticsTest {
	
	private static File gitTestDirectory; 
	
	@BeforeAll
	public static void createGitRepositoryForTest() throws IOException, IllegalStateException, GitAPIException {
        gitTestDirectory = File.createTempFile("cvsm", ".test");        
        Git.init().setDirectory(gitTestDirectory).call();
	}

	@Test
	public void testStatisticsCorrectForAGivenBranch() {
		assertTrue(false);
	}
	
	@AfterAll
	public static void deleteGitRepositoryForTest() throws IOException, IllegalStateException, GitAPIException {
		FileUtils.deleteDirectory(gitTestDirectory);
	}
}
