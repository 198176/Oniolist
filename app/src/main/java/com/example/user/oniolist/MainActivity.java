package com.example.user.oniolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    private DatabaseReference databaseReference;
    private RecyclerView listRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter firebaseAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String emailUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listRecyclerView = (RecyclerView) findViewById(R.id.listRecyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        listRecyclerView.setLayoutManager(linearLayoutManager);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            emailUser = firebaseUser.getEmail();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<ShoppingList> parser = new SnapshotParser<ShoppingList>() {
            @Override
            public ShoppingList parseSnapshot(DataSnapshot dataSnapshot) {
                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);
                if (shoppingList != null) {
                    shoppingList.setId(dataSnapshot.getKey());
                }
                return shoppingList;
            }
        };
        Query query = databaseReference.child("lists").orderByChild("listOwner").equalTo(emailUser);
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
            protected void onBindViewHolder(@NonNull ListViewHolder holder, int position, @NonNull ShoppingList model) {
                holder.listTextView.setText(model.getListName());
            }
        };

        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    listRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        listRecyclerView.setAdapter(firebaseAdapter);
        FloatingActionButton floatingButton = (FloatingActionButton) findViewById(R.id.listFloatingButton);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoppingList shoppingList = new ShoppingList("test", emailUser);
                databaseReference.child("lists").push().setValue(shoppingList);
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

        public ListViewHolder(View v) {
            super(v);
            listTextView = (TextView) itemView.findViewById(R.id.listTextView);
        }
    }
}
