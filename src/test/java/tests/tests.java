package tests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.adiroth.jenkins.plugins.aianalysis.LLMService;
import hudson.model.Run;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class tests {

    @Test
    public void testGetAnswerWithInvalidUrl() {
        // Use an invalid URL so that an error message is returned.
        LLMService service = new LLMService("https://invalid-url", "dummy-token");
        String response = service.getAnswer("Test question");
        assertNotNull("Response should not be null", response);
        assertTrue("Expected response to start with 'Error:'", response.startsWith("Error:"));
    }

    @Test
    public void testGetAnswerWithEmptyQuestion() {
        LLMService service = new LLMService("https://invalid-url", "dummy-token");
        String response = service.getAnswer("");
        assertNotNull("Response should not be null", response);
        // Even an empty question should be processed; since URL is invalid, it should return error.
        assertTrue("Expected response to start with 'Error:'", response.startsWith("Error:"));
    }

    @Test
    public void testGetAnswerWithNullQuestion() {
        LLMService service = new LLMService("https://invalid-url", "dummy-token");
        String response = service.getAnswer(null);
        assertNotNull("Response should not be null", response);
        // JSON may convert null to a string "null" or trigger an exception; we expect an error response.
        assertTrue("Expected response to start with 'Error:'", response.startsWith("Error:"));
    }

    @Test
    public void testAnalysisErrorsMapping() throws Exception {
        // Prepare a dummy log list containing error and non-error lines.
        List<String> logLines = Arrays.asList(
                "This is an info message",
                "Error: Something went wrong",
                "Exception: Null pointer",
                "All is well"
        );

        // Mock the Run object to return the dummy log.
        Run<?, ?> dummyRun = Mockito.mock(Run.class);
        Mockito.when(dummyRun.getLog(1000)).thenReturn(logLines);

        // Create a test LLMService that overrides getAnswer to avoid real HTTP calls.
        LLMService testService = new LLMService("dummy-url", "dummy-token") {
            @Override
            public String getAnswer(String question) {
                // Return a predictable answer for testing purposes.
                return "Test answer for: " + question;
            }
        };

        // Call analysisErrors with the dummy run.
        Map<String, String> results = testService.analysisErrors(dummyRun);

        // Verify that results only contain error/exception lines.
        assertTrue("Should contain error line", results.containsKey("Error: Something went wrong"));
        assertTrue("Should contain exception line", results.containsKey("Exception: Null pointer"));
        assertFalse("Should not contain info message", results.containsKey("This is an info message"));
        assertFalse("Should not contain non-error message", results.containsKey("All is well"));

        // Check that the answers are as expected.
        assertEquals("Test answer for: Error: Something went wrong", results.get("Error: Something went wrong"));
        assertEquals("Test answer for: Exception: Null pointer", results.get("Exception: Null pointer"));
    }
}
