package fr.enoent.firebasedemo;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private SortedList<Task> tasks;

    TaskAdapter() {
        tasks = new SortedList<>(Task.class, new SortedListAdapterCallback<Task>(this) {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }

            @Override
            public boolean areContentsTheSame(Task oldItem, Task newItem) {
                return oldItem.getLabel().equals(newItem.getLabel()) && oldItem.isDone() == newItem.isDone();
            }

            @Override
            public boolean areItemsTheSame(Task item1, Task item2) {
                return item1.getId().equals(item2.getId());
            }
        });


        // Bind to Firebase database
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tasks");
        if (databaseReference != null) {
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Task task = dataSnapshot.getValue(Task.class);
                    task.setId(dataSnapshot.getKey());
                    tasks.add(task);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Task task = dataSnapshot.getValue(Task.class);
                    task.setId(dataSnapshot.getKey());

                    for (int i = 0; i < tasks.size(); ++i) {
                        if (tasks.get(i).getId().equals(task.getId())) {
                            tasks.updateItemAt(i, task);
                            break;
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String removedId = dataSnapshot.getKey();
                    for (int i = 0; i < tasks.size(); ++i) {
                        if (tasks.get(i).getId().equals(removedId)) {
                            tasks.removeItemAt(i);
                            break;
                        }
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    // We're sorting locally anyway
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    void addTask(Task task) {
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference("tasks");
        db.push().setValue(task);
    }

    private void updateTask(Task task) {
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference("tasks");
        db.child(task.getId()).setValue(task);
    }

    private void removeTask(Task task) {
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference("tasks");
        db.child(task.getId()).removeValue();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private CheckBox checkBox;
        private Task task;

        ViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.text);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (task != null && b != task.isDone()) {
                        task.setDone(b);
                        updateTask(task);
                    }
                }
            });

            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (task != null) {
                        removeTask(task);
                        return true;
                    }

                    return false;
                }
            });
        }

        void bind(Task task) {
            this.task = task;
            textView.setText(task.getLabel());
            checkBox.setChecked(task.isDone());
        }
    }
}
