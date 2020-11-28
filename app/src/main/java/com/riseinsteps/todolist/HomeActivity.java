package com.riseinsteps.todolist;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {


    private Toolbar homeToolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private FirebaseUser mUser;
    private String onlineUserID;


    private String key = "";
    private String task = "";
    private String desc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeToolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(homeToolbar);
        getSupportActionBar().setTitle("TODO List App");
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerview);
        floatingActionButton = findViewById(R.id.add_todolist);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        progressDialog = new ProgressDialog(this);

        mUser = mAuth.getCurrentUser();
        onlineUserID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });


    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

        View view = layoutInflater.inflate(R.layout.input_file, null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();


        final EditText task = view.findViewById(R.id.task);
        final EditText desc = view.findViewById(R.id.description);

        Button save = view.findViewById(R.id.save_button);
        Button cancel = view.findViewById(R.id.cancel_button);

        cancel.setBackgroundColor(Color.BLUE);

        save.setBackgroundColor(Color.BLUE);

        cancel.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mTask = task.getText().toString().trim();
                String mDesc = desc.getText().toString().trim();
                String id = reference.push().getKey();
                String data = DateFormat.getInstance().format(new Date());

                if (TextUtils.isEmpty(mTask)) {
                    task.setError("Task Required");
                    return;
                } else if (TextUtils.isEmpty(mDesc)) {
                    desc.setError("Description Required");
                    return;
                } else {
                    progressDialog.setMessage("Adding...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Model model = new Model(mTask, mDesc, id, data);
                    reference.child(id).setValue(model).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        } else {
                            String error = task1.getException().toString();
                            Toast.makeText(HomeActivity.this, "Insertion Failed " + error, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference, Model.class).build();

        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Model model) {
                holder.setDate(model.getDate());
                holder.setDesc(model.getDescripton());
                holder.setTask(model.getTask());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        desc = model.getDescripton();

                        updateTask();

                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieved_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        public void setTask(String task) {
            TextView taskView = myView.findViewById(R.id.textTask);
            taskView.setText(task);
        }

        public void setDesc(String desc) {
            TextView descView = myView.findViewById(R.id.textDesc);
            descView.setText(desc);
        }

        public void setDate(String date) {
            TextView dateView = myView.findViewById(R.id.date);
            dateView.setText(date);
        }
    }

    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

        View view = layoutInflater.inflate(R.layout.update_date, null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();


        final EditText updateTask = view.findViewById(R.id.update_task);
        final EditText updateDesc = view.findViewById(R.id.update_desc);

        updateTask.setText(task);
        updateTask.setSelection(task.length());

        updateDesc.setText(desc);
        updateDesc.setSelection(desc.length());

        Button updateBtn = view.findViewById(R.id.update_button);
        Button deleteBtn = view.findViewById(R.id.delete_button);

        updateBtn.setBackgroundColor(Color.BLUE);
        deleteBtn.setBackgroundColor(Color.BLUE);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = updateTask.getText().toString().trim();
                desc = updateDesc.getText().toString().trim();

                String date = DateFormat.getInstance().format(new Date());

                Model model = new Model(task, desc, key, date);

                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Update Failed " + error, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });

                progressDialog.dismiss();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Task deleted Successful", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Deletion Failed " + error, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
                progressDialog.dismiss();
            }
        });

        dialog.show();


    }
}