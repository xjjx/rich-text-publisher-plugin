package org.korosoft.jenkins.plugin.rtp;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Collection;

/**
 * Rich text portlet for Jenkins Dashboard plugin.
 * <p/>
 * See https://wiki.jenkins-ci.org/display/JENKINS/Dashboard+View for details
 *
 * @author Dmitry Korotkov
 * @since 1.2
 */
public class RichTextPortlet extends DashboardPortlet {
    private String jobName;

    public String getJobName() {
        return jobName;
    }

    public String getRichText() {
        try {
            TopLevelItem item = Jenkins.getInstance().getItem(jobName);
            if (!(item instanceof AbstractProject)) {
                return String.format("Job '%s' not found", jobName);
            }
            AbstractProject<?, ?> project = (AbstractProject<?, ?>) item;
            StringBuilder result = new StringBuilder();
            for (Action action : project.getActions()) {
                if (action instanceof AbstractRichTextAction) {
                    result.append(((AbstractRichTextAction) action).getRichText());
                }
            }
            return result.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @DataBoundConstructor
    public RichTextPortlet(String name, String jobName) {
        super(name);
        this.jobName = jobName;
    }

    public static Collection<String> getAllJobNames() {
        return Jenkins.getInstance().getJobNames();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return "Rich text published within a build";
        }

        public FormValidation doCheckJobName(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error("Please specify job name");
            if (!getAllJobNames().contains(value)) {
                return FormValidation.error(String.format("Job '%s' not found", value));
            }
            return FormValidation.ok();
        }
    }
}
