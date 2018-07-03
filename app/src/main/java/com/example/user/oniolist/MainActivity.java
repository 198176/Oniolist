package com.example.user.oniolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    public static final String IDLIST = "idList";
    public static final String TITLE = "title";
    static SnapshotParser<ShoppingList> parser = new SnapshotParser<ShoppingList>() {
        @Override
        public ShoppingList parseSnapshot(DataSnapshot dataSnapshot) {
            ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);
            if (shoppingList != null) {
                shoppingList.setId(dataSnapshot.getKey());
            }
            return shoppingList;
        }
    };
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseAdapter;
    private String emailUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView listRecyclerView = findViewById(R.id.listRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listRecyclerView.setLayoutManager(linearLayoutManager);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            emailUser = firebaseUser.getEmail();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(ShoppingList.LISTS).orderByChild(ShoppingList.LISTOWNER).equalTo(emailUser);
        FirebaseRecyclerOptions<ShoppingList> options = new FirebaseRecyclerOptions
                .Builder<ShoppingList>().setQuery(query, parser).build();
        firebaseAdapter = new FirebaseRecyclerAdapter<ShoppingList, ListViewHolder>(options) {

            @NonNull
            @Override
            public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new ListViewHolder(inflater.inflate(R.layout.card_list, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull ListViewHolder holder, int position, @NonNull final ShoppingList model) {
                holder.listTextView.setText(model.getListName());
                holder.listTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                        intent.putExtra(IDLIST, model.getId());
                        intent.putExtra(TITLE, model.getListName());
                        startActivity(intent);
                    }
                });
            }
        };

        listRecyclerView.setAdapter(firebaseAdapter);
        FloatingActionButton floatingButton = findViewById(R.id.listFloatingButton);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View mView = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_list_dialog, null);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setView(mView);

                final EditText textDialog = mView.findViewById(R.id.nameListDialog);
                alertDialog.setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                String nameList = textDialog.getText().toString().trim();
                                if (nameList.length() > 0) {
                                    ShoppingList shoppingList = new ShoppingList(nameList, emailUser);
                                    databaseReference.child(ShoppingList.LISTS).push().setValue(shoppingList);
                                } else {
                                    Toast.makeText(MainActivity.this, R.string.list_must_have_name, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })

                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

                alertDialog.create().show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        firebaseAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAdapter.startListening();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView listTextView;

        ListViewHolder(View v) {
            super(v);
            listTextView = itemView.findViewById(R.id.listTextView);
        }
    }
}
