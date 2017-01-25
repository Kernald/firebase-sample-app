package fr.enoent.firebasedemo;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class MainActivity extends AppCompatActivity {
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Activate Firebase Database persistance
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // UI setup
        setContentView(R.layout.activity_main);
        adapter = new TaskAdapter();
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        // Initialize remote config default values
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Trigger a remote config fetch
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            remoteConfig.activateFetched();
                        } else {
                            Log.e("MainActivity", "Fetch failed", task.getException());
                            Toast.makeText(MainActivity.this, "Fetch Failed", Toast.LENGTH_SHORT).show();
                        }
                        remoteConfigLoaded();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MainActivity", "Fetch failed", e);
                    }
                });
    }

    private void remoteConfigLoaded() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        // A new data set has been fetched: update the title
        setTitle(remoteConfig.getString("title"));
    }

    private void createTask() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_task_dialog_title)
                .setView(input)
                .setNegativeButton(R.string.new_task_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                .setPositiveButton(R.string.new_task_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.addTask(new fr.enoent.firebasedemo.Task(validateInput(input.getText().toString()), false));
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private static String validateInput(String input) {
        final String trimmed = input.trim();
        if (trimmed.toLowerCase().contains("iphone")) {
            throw new RuntimeException("Invalid input");
        }

        return trimmed;
    }
}
