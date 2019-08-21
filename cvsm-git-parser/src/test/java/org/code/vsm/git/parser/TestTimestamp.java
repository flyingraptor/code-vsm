package org.code.vsm.git.parser;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jgit.util.time.ProposedTimestamp;

public class TestTimestamp extends ProposedTimestamp {
	
	private long milliseconds;
	
	public TestTimestamp(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	@Override
	public long read(TimeUnit unit) {
		return milliseconds;
	}

	@Override
	public void blockUntil(Duration maxWait) throws InterruptedException, TimeoutException {
		return;
	}

}
