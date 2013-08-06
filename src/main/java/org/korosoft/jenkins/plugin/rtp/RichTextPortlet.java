package org.korosoft.jenkins.plugin.rtp;

/*

The New BSD License

Copyright (c) 2011-2013, Dmitry Korotkov
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this
  list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

- Neither the name of the Jenkins RuSalad Plugin nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

import hudson.Extension;
import hudson.model.*;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Collection;
import java.util.List;

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
    private boolean useLastStable;

    public String getJobName() {
        return jobName;
    }

    public boolean isUseLastStable() {
        return useLastStable;
    }

    public String getRichText() {
        try {
            TopLevelItem item = Jenkins.getInstance().getItem(jobName);
            if (!(item instanceof AbstractProject)) {
                return String.format(Messages.jobNotFound(), jobName);
            }
            AbstractProject<?, ?> project = (AbstractProject<?, ?>) item;
            if (!useLastStable) {
                return getRichTextFromActions(project.getActions());
            } else {
                for (AbstractBuild<?, ?> abstractBuild : project.getBuilds()) {
                    if (abstractBuild.getResult().isBetterOrEqualTo(Result.SUCCESS)) {
                        return getRichTextFromActions(abstractBuild.getActions());
                    }
                }
                return Messages.noStableBuildsYet();
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String getRichTextFromActions(List<Action> actions) {
        StringBuilder result = new StringBuilder();
        for (Action action : actions) {
            if (action instanceof AbstractRichTextAction) {
                result.append(((AbstractRichTextAction) action).getRichText());
            }
        }
        return result.toString();
    }

    @DataBoundConstructor
    public RichTextPortlet(String name, String jobName, boolean useLastStable) {
        super(name);
        this.jobName = jobName;
        this.useLastStable = useLastStable;
    }

    public static Collection<String> getAllJobNames() {
        return Jenkins.getInstance().getJobNames();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return Messages.buildPortletTitle();
        }

        public FormValidation doCheckJobName(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error(Messages.noJobName());
            if (!getAllJobNames().contains(value)) {
                return FormValidation.error(String.format(Messages.jobNotFound(), value));
            }
            return FormValidation.ok();
        }
    }
}
