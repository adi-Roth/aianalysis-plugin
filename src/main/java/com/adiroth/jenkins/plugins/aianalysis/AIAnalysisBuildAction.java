package com.adiroth.jenkins.plugins.aianalysis;

import hudson.Extension;
import hudson.model.Run;
import jenkins.model.RunAction2;
import com.cloudbees.plugins.credentials.common.StringCredentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.security.ACL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Extension
public class AIAnalysisBuildAction implements RunAction2 {
    private transient Run<?, ?> run;
    private LLMService llmService;

    @Override
    public void setRun(Run<?, ?> run) {
        this.run = run;
        // Retrieve the job property.
        AIAnalysisJobProperty property = run.getParent().getProperty(AIAnalysisJobProperty.class);
        if (property != null && property.isEnabled()) {
            String llmUrl = property.getLlmUrl();
            String credentialsId = property.getCredentialsId();
            String token = getTokenFromCredentials(run, credentialsId);
            if (token != null && !llmUrl.isEmpty()) {
                llmService = new LLMService(llmUrl, token);
            }
        }
    }

    public Run<?, ?> getRun() {
        return run;
    }

    /**
     * Only display the sidebar link if the property is enabled and LLMService is configured.
     */
    public String getIconFileName() {
        if (run == null) {
            return null;
        }
        AIAnalysisJobProperty property = run.getParent().getProperty(AIAnalysisJobProperty.class);
        if (property == null || !property.isEnabled() || llmService == null) {
            return null;
        }
        return "/plugin/aianalysis/images/ai-icon.png";
    }

    public String getDisplayName() {
        return "AI Analysis";
    }

    public String getUrlName() {
        return "aiAnalysis";
    }

    /**
     * Calls the LLMService to analyze the build log for errors/exceptions.
     */
    public Map<String, String> getAnalysisResults() {
        if (llmService == null) {
            return null;
        }
        return llmService.analysisErrors(run);
    }

    /**
     * Uses the Jenkins Credentials API to retrieve the token from a secret text credential.
     */
    private String getTokenFromCredentials(Run<?, ?> run, String credentialsId) {
        if (credentialsId == null || credentialsId.isEmpty()) {
            return null;
        }
        List<StringCredentials> creds = CredentialsProvider.lookupCredentials(
                StringCredentials.class,
                run.getParent(),
                ACL.SYSTEM,
                Collections.emptyList());
        for (StringCredentials cred : creds) {
            if (cred.getId().equals(credentialsId)) {
                return cred.getSecret().getPlainText();
            }
        }
        return null;
    }
}
