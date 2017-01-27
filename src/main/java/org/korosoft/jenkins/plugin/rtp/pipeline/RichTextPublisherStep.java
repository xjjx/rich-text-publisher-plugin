package org.korosoft.jenkins.plugin.rtp.pipeline;

import hudson.Extension;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
//import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.HttpResponse;
import org.korosoft.jenkins.plugin.rtp.MarkupParser;
import org.korosoft.jenkins.plugin.rtp.RichTextPublisher;
import org.korosoft.jenkins.plugin.rtp.RichTextPublisher.DescriptorImpl;


public class RichTextPublisherStep extends AbstractStepImpl {
	
	private static final Log log = LogFactory.getLog(RichTextPublisherStep.class);
	
	private String stableText;
	private String unstableText;
	private String failedText;
	private String parserName;
	private boolean unstableAsStable;
	private boolean failedAsStable;
	
	private transient MarkupParser markupParser;
	
	//RichTextPublisher rtp = null;
	
	RichTextPublisherStepExecution test = new RichTextPublisherStepExecution();
	
	@DataBoundConstructor
	public RichTextPublisherStep() {
		//rtp = new RichTextPublisher(stableText, unstableText, failedText, unstableAsStable, failedAsStable, parserName);
	}
	
	//public RichTextPublisher getRTP() {
	//	return rtp;
	//}
	
	public String getStableText() {
	    return stableText;
	}
	@DataBoundSetter 
	public void setStableText(String stableText) {
		this.stableText = stableText==null? null:stableText;
	}
	
	public String getUnstableText() {
        return unstableText;
    }
	@DataBoundSetter
    public void setUnstableText(String unstableText) {
		this.unstableText = unstableText==null? null:unstableText;
    }
	
	public String getFailedText() {
	    return failedText;
	}
	@DataBoundSetter 
	public void setFailedText(String failedText) {
		this.failedText = failedText==null? null:failedText;
	}
	
	public boolean getIsUnstableAsStable() {
        return unstableAsStable;
    }
    @DataBoundSetter
    public void setIsUnstableAsStable(boolean unstableAsStable) {
        this.unstableAsStable = unstableAsStable;
    }

    public boolean getIsFailedAsStable() {
        return failedAsStable;
    }
    @DataBoundSetter
    public void setIsFailedAsStable(boolean failedAsStable) {
        this.failedAsStable = failedAsStable;
    }
    
    public String getParserName() {
        return parserName;
    }
    @DataBoundSetter
    public void setParserName(String parserName) {
        if (parserName == null) {
            parserName = "HTML";
        }
        this.parserName = parserName;
        this.markupParser = DescriptorImpl.markupParsers.get(parserName);
    }
    
    public MarkupParser getMarkupParser() {
        if (markupParser == null) {
            markupParser = DescriptorImpl.markupParsers.get(parserName);
        }
        return markupParser;
    }
 
    
	@Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

		private static transient Map<String, MarkupParser> markupParsers;
	    private static transient List<String> markupParserNames;
		
	    static {
            loadParsers();
        }
	    
        public DescriptorImpl() {
            super(RichTextPublisherStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "richTextPublisher";
        }

        @Override
        public String getDisplayName() {
            return "Publish some rich text";
        }
        
        public HttpResponse doFillParserNameItems() {
            loadParsers();
            ListBoxModel model = new ListBoxModel();
            for (String name : markupParserNames) {
                model.add(name, name);
            }
            return model;
        }
        
        private static void loadParsers() {
            Properties properties = new Properties();
            InputStream stream = DescriptorImpl.class.getResourceAsStream("/parsers.properties");
            try {
                properties.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(stream);
            }

            markupParsers = new HashMap<String, MarkupParser>();
            markupParserNames = new ArrayList<String>();
            for (Object o : properties.values()) {
                try {
                    MarkupParser parser = (MarkupParser) Class.forName(o.toString()).newInstance();
                    String name = parser.getName();
                    markupParserNames.add(name);
                    markupParsers.put(name, parser);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }
}