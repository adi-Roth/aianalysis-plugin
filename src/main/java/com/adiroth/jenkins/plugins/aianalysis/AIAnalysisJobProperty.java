package com.adiroth.jenkins.plugins.aianalysis;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;

public class AIAnalysisJobProperty extends JobProperty<AbstractProject<?, ?>> {
    private final boolean enabled;
    private final String llmUrl;
    private final String credentialsId;

    @DataBoundConstructor
    public AIAnalysisJobProperty(boolean enabled, String llmUrl, String credentialsId) {
        this.enabled = enabled;
        this.llmUrl = llmUrl;
        this.credentialsId = credentialsId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getLlmUrl() {
        return llmUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return "Enable AI Analysis";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public AIAnalysisJobProperty newInstance(StaplerRequest req, JSONObject formData) {
            boolean enabled = formData.optBoolean("enabled", false);
            String llmUrl = formData.optString("llmUrl", "");
            String credentialsId = formData.optString("credentialsId", "");
            return new AIAnalysisJobProperty(enabled, llmUrl, credentialsId);
        }
    }
}
