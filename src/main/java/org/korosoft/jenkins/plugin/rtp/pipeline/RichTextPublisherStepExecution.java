package org.korosoft.jenkins.plugin.rtp.pipeline;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.korosoft.jenkins.plugin.rtp.RichTextPublisher;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

public class RichTextPublisherStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {
	
	@StepContextParameter
    private transient TaskListener listener;
	
	@Inject
    private transient RichTextPublisherStep step;
	
	@StepContextParameter
	private transient FilePath workspace;
	
	@StepContextParameter
    private transient Run<?,?> build;

    @StepContextParameter
    private transient Launcher launcher;
	
	@Override
	protected Void run() throws Exception {
		
		RichTextPublisher rtp = new RichTextPublisher(step.getStableText(), step.getUnstableText(), step.getFailedText(), step.getIsUnstableAsStable(), step.getIsFailedAsStable(), step.getParserName());
		rtp.perform(build, workspace, launcher, listener);
		return null;
	}
}