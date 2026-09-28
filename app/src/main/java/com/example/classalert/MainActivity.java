package com.example.classalert;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String DOCX_URL =
            "https://raw.githubusercontent.com/surioalex13-maker/Project-In-App-Dev/main/ClassAlert.docx";

    private TextView contentText;
    private TextView statusText;
    private TextView progressText;
    private TextView documentInfoText;
    private Button downloadButton;
    private Button loadCachedButton;
    private Button deleteButton;
    private ProgressBar progressBar;

    private final OkHttpClient client = new OkHttpClient();
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        contentText = findViewById(R.id.contentText);
        statusText = findViewById(R.id.statusText);
        progressText = findViewById(R.id.progressText);
        documentInfoText = findViewById(R.id.documentInfoText);
        downloadButton = findViewById(R.id.downloadButton);
        loadCachedButton = findViewById(R.id.loadCachedButton);
        deleteButton = findViewById(R.id.deleteButton);
        progressBar = findViewById(R.id.progressBar);

        // Set up button listeners
        downloadButton.setOnClickListener(v -> downloadAndSaveDocument());
        loadCachedButton.setOnClickListener(v -> loadCachedDocument());
        deleteButton.setOnClickListener(v -> deleteCachedDocument());

        // Check if file is already cached
        updateCacheStatus();
    }

    /**
     * Update cache status and button states
     */
    private void updateCacheStatus() {
        boolean isCached = FileManager.isCached(this);
        loadCachedButton.setEnabled(isCached);
        deleteButton.setEnabled(isCached);

        if (isCached) {
            String info = FileManager.getFileInfo(this);
            documentInfoText.setText(info);
            statusText.setText("Document cached locally. Ready to load.");
        } else {
            documentInfoText.setText("");
            statusText.setText("Ready to download document");
        }
    }

    /**
     * Download and save document from GitHub
     */
    private void downloadAndSaveDocument() {
        if (isProcessing) {
            Toast.makeText(this, "Operation in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessing = true;
        setLoadingState(true);
        progressText.setText("Downloading from GitHub...");
        progressText.setVisibility(View.VISIBLE);
        statusText.setText("Downloading...");

        Request request = new Request.Builder()
                .url(DOCX_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isProcessing = false;
                    setLoadingState(false);
                    progressText.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    statusText.setText("Download failed. Try again.");
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        isProcessing = false;
                        setLoadingState(false);
                        progressText.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Download failed: Bad response", Toast.LENGTH_LONG).show();
                        statusText.setText("Download failed.");
                    });
                    return;
                }

                try {
                    // Save to internal storage
                    InputStream inputStream = response.body().byteStream();
                    FileManager.saveToInternalStorage(MainActivity.this, inputStream);

                    runOnUiThread(() -> {
                        progressText.setText("Reading document...");
                        statusText.setText("Processing document...");
                    });

                    // Read the saved file
                    readAndDisplayDocument();

                    runOnUiThread(() -> {
                        isProcessing = false;
                        setLoadingState(false);
                        progressText.setVisibility(View.GONE);
                        updateCacheStatus();
                        Toast.makeText(MainActivity.this, "Document downloaded and saved successfully!", Toast.LENGTH_LONG).show();
                        statusText.setText("Document loaded from cache.");
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        isProcessing = false;
                        setLoadingState(false);
                        progressText.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        statusText.setText("Error processing document.");
                    });
                }
            }
        });
    }

    /**
     * Load cached document from internal storage
     */
    private void loadCachedDocument() {
        if (isProcessing) {
            Toast.makeText(this, "Operation in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!FileManager.isCached(this)) {
            Toast.makeText(this, "No cached file found", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessing = true;
        setLoadingState(true);
        progressText.setText("Loading cached document...");
        progressText.setVisibility(View.VISIBLE);
        statusText.setText("Loading...");

        // Load in background thread
        new Thread(() -> {
            try {
                readAndDisplayDocument();
                runOnUiThread(() -> {
                    isProcessing = false;
                    setLoadingState(false);
                    progressText.setVisibility(View.GONE);
                    statusText.setText("Document loaded from cache.");
                    Toast.makeText(MainActivity.this, "Document loaded successfully!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isProcessing = false;
                    setLoadingState(false);
                    progressText.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    statusText.setText("Error loading document.");
                });
            }
        }).start();
    }

    /**
     * Read and display document content
     */
    private void readAndDisplayDocument() throws Exception {
        InputStream inputStream = FileManager.readFromCache(this);
        String text = DocxReader.readDocxFromStream(inputStream);
        inputStream.close();

        // Get document statistics
        inputStream = FileManager.readFromCache(this);
        DocxReader.DocumentStats stats = DocxReader.getDocumentStats(inputStream);
        inputStream.close();

        runOnUiThread(() -> {
            contentText.setText(text);
            String statsText = "Document Stats: " + stats.toString();
            if (!documentInfoText.getText().toString().isEmpty()) {
                documentInfoText.setText(documentInfoText.getText() + "\n" + statsText);
            } else {
                documentInfoText.setText(statsText);
            }
        });
    }

    /**
     * Delete cached document
     */
    private void deleteCachedDocument() {
        if (FileManager.deleteCachedFile(this)) {
            Toast.makeText(this, "Cached file deleted", Toast.LENGTH_SHORT).show();
            contentText.setText("No document loaded. Click 'Download & Save Document' to get started.");
            documentInfoText.setText("");
            updateCacheStatus();
            statusText.setText("Cache cleared.");
        } else {
            Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set loading state for UI
     */
    private void setLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        downloadButton.setEnabled(!loading);
        loadCachedButton.setEnabled(!loading && FileManager.isCached(this));
        deleteButton.setEnabled(!loading && FileManager.isCached(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}