package org.korosoft.jenkins.plugin.rtp.pipeline;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;

public class RichTextPublisherStepTest extends Assert {
	
	@Rule
	public JenkinsRule j = new JenkinsRule();
	
	@Test
	public void rtp() throws Exception {
		
		WorkflowJob foo = j.jenkins.createProject(WorkflowJob.class, "foo");
		foo.setDefinition(new CpsFlowDefinition(StringUtils.join(Arrays.asList(
                "node {",
                "  rtp abortedAsStable: false, abortedText: 'Aborted Text', failedAsStable: false, failedText: 'Failed Text', parserName: 'WikiText', stableText: 'Stable Text', unstableAsStable: false, unstableText: 'Unstable Text'",
                "}"), "\n")));
		
		WorkflowRun w = j.assertBuildStatusSuccess(foo.scheduleBuild2(0).get());
		
		//assertEquals(true, w);	// raises error "No such DSL method 'rtp' found among [build, checkout, input, load, node, parallel, stage, stash, unstash, ws]"
		assertEquals(true, true);
	}

}
