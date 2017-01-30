package org.korosoft.jenkins.plugin.rtp.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.korosoft.jenkins.plugin.rtp.AbstractRichTextAction;
import org.korosoft.jenkins.plugin.rtp.BuildRichTextAction;

import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.model.TransientActionFactory;

public class RichTextPublisherStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

	private static final transient Pattern FILE_VAR_PATTERN = Pattern.compile("\\$\\{(file|file_sl):([^\\}]+)\\}", Pattern.CASE_INSENSITIVE);
	
	@StepContextParameter
    private transient TaskListener listener;
	
	@Inject
    private transient RichTextPublisherStep step;
	
	@Inject 
	StepContext context;
	
	@StepContextParameter
    private transient Run<?,?> build;

    @StepContextParameter
    private transient Launcher launcher;
	
	@Override
	protected Void run() throws Exception {
		//listener.getLogger().println("Rich text publisher called!");
		//listener.getLogger().println("StableText: "+step.getStableText());
		//listener.getLogger().println("UnstableText: "+step.getUnstableText());
		//listener.getLogger().println("FailedText: "+step.getFailedText());
		//listener.getLogger().println("ParserName: "+step.getParserName());
		//listener.getLogger().println("UnstableAsStable: "+step.getIsUnstableAsStable());
		//listener.getLogger().println("FailedAsStable: "+step.getIsFailedAsStable());
		
		final String text;
		if (build.getResult() == null)
		{
			listener.getLogger().println("Build result is null! Setting build to FAILURE!");
			build.setResult(Result.FAILURE);
		}
        if (build.getResult().isBetterOrEqualTo(Result.SUCCESS)) {
            text = step.getStableText();
        } else if (build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
            text = step.getIsUnstableAsStable() ? step.getStableText() : step.getUnstableText();
        } else {
            text = step.getIsFailedAsStable() ? step.getStableText() : step.getFailedText();
        }

        Map<String, String> vars = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : build.getEnvironment(listener).entrySet()) {
            vars.put(String.format("ENV:%s", entry.getKey()), entry.getValue());
        }
        Map<String, String> parentvars = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : build.getParent().getEnvironment(null, listener).entrySet()) {
        	parentvars.put(String.format("ENV:%s", entry.getKey()), entry.getValue());
        }
        
        //vars.putAll(build.getBuildVariables()); 			// not working, but original code,
        // if there is an alternative this all could be integrated into RichTextPublisher.java and you could call those functions
        //parentvars.putAll(build.getBuildVariables());
        
        Matcher matcher = FILE_VAR_PATTERN.matcher(text);
        int start = 0;
        while (matcher.find(start)) {
            String fileName = matcher.group(2);
            FilePath filePath = new FilePath(context.get(FilePath.class), fileName);
            if (filePath.exists()) {
                String value = filePath.readToString();
                if (matcher.group(1).length() != 4) { // Group is file_sl
                    value = value.replace("\n", "").replace("\r", "");
                }
                String key = String.format("%s:%s", matcher.group(1), fileName);
                vars.put(key, value);
                parentvars.put(key, value);
            }
            start = matcher.end();
        }
        
        AbstractRichTextAction action = new BuildRichTextAction(build, step.getMarkupParser().parse(replaceVars(text, vars)));
        AbstractRichTextAction parentaction = new BuildRichTextAction(build, step.getMarkupParser().parse(replaceVars(text, parentvars)));

        //idk if this could be considered as a workaround for adding the text to pipeline page
        build.getParent().replaceAction(parentaction);
        build.getParent().save();
        build.addAction(action);
        build.save();

        return null;
	}
	
	private String replaceVars(String publishText, Map<String, String> vars) {

        for (Map.Entry<String, String> var : vars.entrySet()) {
            String key = String.format("${%s}", var.getKey());
            String value = var.getValue();
            publishText = publishText.replace(key, value);
        }
        return publishText;
    }
}