package org.code.vsm.git.parser;

public interface BranchMetricsCalculator {
	Integer getAverageMeanCommitWaitingTimeInSeconds(String string);
}
