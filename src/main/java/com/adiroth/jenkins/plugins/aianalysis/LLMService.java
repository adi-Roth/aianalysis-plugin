package com.adiroth.jenkins.plugins.aianalysis;

import hudson.model.Run;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLMService {
    private String llmUrl;
    private String token;

    public LLMService(String llmUrl, String token) {
        this.llmUrl = llmUrl;
        this.token = token;
    }

    /**
     * Sends the provided question to the LLM service and returns the answer.
     * SSL certificate checking is disabled.
     */
    public String getAnswer(String question) {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build(), NoopHostnameVerifier.INSTANCE);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();

            HttpPost post = new HttpPost(llmUrl + "/getAnswer");
            post.addHeader("Content-Type", "application/json");
            post.addHeader("Authorization", "Bearer " + token);

            JSONObject json = new JSONObject();
            json.put("question", question);
            StringEntity entity = new StringEntity(json.toString(), StandardCharsets.UTF_8);
            post.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(post);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    JSONObject jsonResponse = new JSONObject(result);
                    return jsonResponse.optString("answer", "No answer provided");
                } else {
                    return "Error: Received HTTP status " + statusCode;
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Scans the build log (up to 1000 lines) for lines containing "error" or "exception"
     * and calls getAnswer() for each, returning a mapping of the log line to the answer.
     */
    public Map<String, String> analysisErrors(Run<?, ?> run) {
        Map<String, String> results = new HashMap<>();
        try {
            List<String> logLines = run.getLog(1000);
            for (String line : logLines) {
                if (line.toLowerCase().contains("error") || line.toLowerCase().contains("exception")) {
                    String answer = getAnswer(line);
                    results.put(line, answer);
                }
            }
        } catch (Exception e) {
            results.put("AnalysisError", "Error analyzing build log: " + e.getMessage());
        }
        return results;
    }
}
