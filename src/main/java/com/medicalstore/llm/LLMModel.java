package com.medicalstore.llm;

/**
 * Simple placeholder for a language model integration.
 * In a real scenario this could wrap an API client to an LLM service.
 */
public class LLMModel {
    /**
     * Generates a dummy response for the given prompt.
     * 
     * @param prompt the input text
     * @return a placeholder response string
     */
    public String generateResponse(String prompt) {
        // TODO: integrate with actual LLM service
        return "Response to: " + prompt;
    }
}
