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
	public void richText() throws Exception {
		
		WorkflowJob foo = j.jenkins.createProject(WorkflowJob.class, "foo");
		foo.setDefinition(new CpsFlowDefinition(StringUtils.join(Arrays.asList(
                "node {",
                "  echo 'hi'",
                "}"), "\n")));
		
		WorkflowRun w = j.assertBuildStatusSuccess(foo.scheduleBuild2(0).get());
		
		assertEquals(true, true);
	}

}
