package com.filantrop.androidworkmanagerexample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private MainViewModel mainViewModel;

    private Button loadSniButton;
    private Button deleteSniButton;
    private Button autoSelectSniButton;
    private View currentSniLayout;
    private TextView currentSniText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initialize buttons
        loadSniButton = findViewById(R.id.load_sni_button);
        deleteSniButton = findViewById(R.id.delete_sni_button);
        autoSelectSniButton = findViewById(R.id.auto_select_sni_button);

        // Set click listeners
        loadSniButton.setOnClickListener(v -> mainViewModel.handleLoadSniClick());

        deleteSniButton.setOnClickListener(v -> mainViewModel.handleDeleteSniClick());

        autoSelectSniButton.setOnClickListener(v -> mainViewModel.handleAutoSelectSniClick());

        // Initialize current SNI layout
        currentSniLayout = findViewById(R.id.current_sni_layout);
        currentSniText = findViewById(R.id.SNI_text_field);

        // Add click listener to current SNI layout
        currentSniLayout.setOnClickListener(v -> {
            Log.d(TAG, "Current SNI layout clicked");
            showChangeSniDialog();
        });

        // Observe ViewModel LiveData if needed
        observeViewModel();
    }

    private void observeViewModel() {
        mainViewModel.getCurrentSni().observe(this, sniValue -> {
            Log.d(TAG, "SNI value updated: " + sniValue);
            currentSniText.setText(sniValue);
        });
    }

    private void showChangeSniDialog() {
        Log.d(TAG, "Showing change SNI dialog");

        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change SNI");

        // Create EditText for input
        final EditText input = new EditText(this);
        input.setHint("Enter new SNI value");

        // Set current SNI value as default in the dialog
        mainViewModel.getCurrentSni().observe(this, currentValue -> {
            if (currentValue != null) {
                input.setText(currentValue);
            }
        });

        builder.setView(input);

        // Add buttons
        builder.setPositiveButton("OK", (dialog, which) -> mainViewModel.updateCurrentSni(input.getText().toString().trim()));
        builder.setNeutralButton(getString(R.string.reset_default_button), (dialog, which) -> mainViewModel.resetSniToDefult());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}