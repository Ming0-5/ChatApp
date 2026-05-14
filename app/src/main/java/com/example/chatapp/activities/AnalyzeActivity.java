package com.example.chatapp.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.databinding.ActivityAnalyzeBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * AnalyzeActivity is responsible for displaying and processing message risk analysis.
 * It sends a list of chat messages to a Python-based backend (Flask) which performs:
 * - Sentiment Analysis
 * - Sensitive Word Detection
 * - Grooming/Risk Assessment
 * - Contextual AI Analysis
 * This activity uses Volley for network communication and View Binding for UI interaction.
 */
public class AnalyzeActivity extends AppCompatActivity {

    /**
     * Backend endpoint URL.
     * Use 10.0.2.2 to access localhost from the Android Emulator.
     */
    private static final String BACKEND_URL = "http://10.0.2.2:5000/analyze";

    /**
     * Intent extra key for passing messages to this activity.
     */
    public static final String KEY_MESSAGES = "messages";

    private ActivityAnalyzeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityAnalyzeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUI();
        processIntent();
    }

    /**
     * Initializes UI elements and click listeners.
     */
    private void initUI() {
        binding.buttonBack.setOnClickListener(v -> finish());
    }

    /**
     * Retrieves messages from the intent and triggers the backend analysis.
     */
    private void processIntent() {
        ArrayList<String> messages = getIntent().getStringArrayListExtra(KEY_MESSAGES);

        if (messages == null) {
            messages = new ArrayList<>();
        }

        // Display the raw messages being analyzed
        binding.textMessage.setText(messages.toString());

        // Trigger the analysis request
        sendToBackend(messages);
    }

    /**
     * Sends the collected messages to the Flask backend for AI processing.
     *
     * @param messages The list of chat messages to be analyzed.
     */
    private void sendToBackend(ArrayList<String> messages) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray(messages);
            jsonObject.put("messages", jsonArray);

            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BACKEND_URL,
                    jsonObject,
                    this::handleBackendResponse,
                    error -> {
                        binding.textRisk.setText("Backend Connection Failed");
                        error.printStackTrace();
                    }
            );

            queue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the successful response from the backend and updates the UI.
     *
     * @param response The JSON response containing analysis results.
     */
    private void handleBackendResponse(JSONObject response) {
        try {
            // Extract core analysis data
            String sentiment = response.getString("sentiment");
            double sentimentScore = response.getDouble("sentiment_score");
            String risk = response.getString("risk");
            int percentage = response.getInt("percentage");
            String context = response.optString("contextual_analysis", "No contextual analysis available.");

            // Extract detected words and phrases
            JSONArray detectedWords = response.getJSONArray("detected_words");
            JSONArray phrases = response.getJSONArray("detected_phrases");

            // Update UI with formatted strings
            binding.textSentiment.setText(String.format(Locale.getDefault(), "Sentiment: %s (%.2f)", sentiment, sentimentScore));
            
            String sensitiveAnalysis = String.format(Locale.getDefault(),
                    "Sensitive Words:\n%s\n\nDetected Grooming Phrases:\n%s",
                    detectedWords.toString(), phrases.toString());
            binding.textSensitiveWords.setText(sensitiveAnalysis);

            binding.textPercentage.setText(String.format(Locale.getDefault(), "Risk Score: %d%%", percentage));
            binding.textRisk.setText(String.format(Locale.getDefault(), "Assessment: %s", risk));
            binding.textContext.setText(String.format(Locale.getDefault(), "AI Context Analysis:\n%s", context));

        } catch (Exception e) {
            binding.textRisk.setText("Data Parsing Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
