package org.korosoft.jenkins.plugin.rtp.pipeline;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import hudson.model.TaskListener;

public class RichTextPublisherStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

	@StepContextParameter
    private transient TaskListener listener;
	
	@Inject
    private transient RichTextPublisherStep step;
	
	//private final static Logger LOG = TaskListener.getLogger();
	
	@Override
	protected Void run() throws Exception {
		listener.getLogger().println("Rich text publisher called!");
		listener.getLogger().println("StableText: "+step.getStableText());
		listener.getLogger().println("UnstableText: "+step.getUnstableText());
		listener.getLogger().println("FailedText: "+step.getFailedText());
		listener.getLogger().println("ParserName: "+step.getParserName());
		listener.getLogger().println("UnstableAsStable: "+step.getIsUnstableAsStable());
		listener.getLogger().println("FailedAsStable: "+step.getIsFailedAsStable());
		return null;
	}
}