package com.filantrop.androidworkmanagerexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private MainViewModel mainViewModel;

    private Button loadSniButton;
    private Button deleteSniButton;
    private Button autoSelectSniButtonStart;
    private Button autoSelectSniButtonStop;
    private View currentSniLayout;
    private View sniProgressLayout;
    private TextView currentSniText;
    private TextView progressTextValue;

    private AlertDialog autoSelectDialog;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initialize buttons
        loadSniButton = findViewById(R.id.load_sni_button);
        deleteSniButton = findViewById(R.id.delete_sni_button);

        // Set click listeners
        loadSniButton.setOnClickListener(v -> onLoadButtonClicked());
        deleteSniButton.setOnClickListener(v -> onDeleteButtonClicked());

        // Initialize current SNI layout
        currentSniLayout = findViewById(R.id.current_sni_layout);
        currentSniText = findViewById(R.id.SNI_text_field);

        // Add click listener to current SNI layout
        currentSniLayout.setOnClickListener(v -> {
            Log.d(TAG, "Current SNI layout clicked");
            showChangeSniDialog();
        });

        sniProgressLayout = findViewById(R.id.sni_progress_layout);
        autoSelectSniButtonStart = findViewById(R.id.auto_select_sni_button_start);
        autoSelectSniButtonStop = findViewById(R.id.auto_select_sni_button_stop);

        mainViewModel.getSearchInProgress().observe(this, (inProgress) -> {
            autoSelectSniButtonStart.setEnabled(!inProgress);
            autoSelectSniButtonStop.setEnabled(inProgress);
            sniProgressLayout.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        });

        autoSelectSniButtonStart.setOnClickListener(v->{
            showAutoSelectDialog();
        });

        autoSelectSniButtonStop.setOnClickListener(v->{
            mainViewModel.stopSNISearch();
        });

        // Observe ViewModel LiveData if needed
        observeViewModel();

        // Register the activity result launcher
        // This must be done in onCreate or as a class member initializer.
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            mainViewModel.readFileContent(this, data.getData());
                        }
                    } else {
                        Log.w(TAG, "File selection cancelled.");
                    }
                });
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
        builder.setNeutralButton(getString(R.string.reset_default_button), (dialog, which) -> mainViewModel.resetSniToDefault());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAutoSelectDialog() {
        // Prevent creating multiple dialogs
        if (autoSelectDialog != null && autoSelectDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate the custom layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_autoselect_sni, null);
        builder.setView(dialogView);

        // --- Setup Spinner ---
        Spinner serverSpinner = dialogView.findViewById(R.id.dialog_server_spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mainViewModel.getServers());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverSpinner.setAdapter(spinnerAdapter);
        // --- End Spinner Setup ---

        // --- Setup Buttons ---
        Button buttonCancel = dialogView.findViewById(R.id.dialog_button_cancel);
        Button buttonStart = dialogView.findViewById(R.id.dialog_button_start);

        // Create the dialog before setting click listeners to allow for dismissing it
        autoSelectDialog = builder.create();

        buttonCancel.setOnClickListener(v -> {
            Log.d(TAG, "Auto-select dialog cancelled.");
            autoSelectDialog.dismiss();
        });

        buttonStart.setOnClickListener(v -> {
            // Get the originally selected server object
            int selectedPosition = serverSpinner.getSelectedItemPosition();
            String selectedServer = mainViewModel.getServers().get(selectedPosition);

            mainViewModel.startSNISearch(selectedServer);
            autoSelectDialog.dismiss();
        });

        autoSelectDialog.show();
    }


    private void onDeleteButtonClicked() {
        mainViewModel.deleteAllSni();
        Toast.makeText(this, "All loaded SNI have been deleted.", Toast.LENGTH_SHORT).show();
    }

    private void onLoadButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            // Launch the intent using the ActivityResultLauncher
            filePickerLauncher.launch(Intent.createChooser(intent, "Select a SNI file"));
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially handle the case where the device has no file manager
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }


}