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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.example.user.oniolist.MainActivity.parser;

public class ProductActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter firebaseAdapter;
    private String idList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        idList = intent.getStringExtra(MainActivity.IDLIST);
        setTitle(intent.getStringExtra(MainActivity.TITLE));

        RecyclerView listRecyclerView = findViewById(R.id.listRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listRecyclerView.setLayoutManager(linearLayoutManager);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(ShoppingList.LISTS).child(idList).child(ShoppingList.PRODUCTS);
        FirebaseRecyclerOptions<ShoppingList> options = new FirebaseRecyclerOptions
                .Builder<ShoppingList>().setQuery(query, parser).build();
        firebaseAdapter = new FirebaseRecyclerAdapter<ShoppingList, ProductActivity.ListViewHolder>(options) {

            @NonNull
            @Override
            public ProductActivity.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new ProductActivity.ListViewHolder(inflater.inflate(R.layout.card_product, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull ProductActivity.ListViewHolder holder,
                                            int position, @NonNull final ShoppingList model) {
                holder.listCheckBox.setText(model.getListName());
                holder.listCheckBox.setChecked(model.isBought());
                holder.listCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        model.setBought(isChecked);
                        databaseReference.child(ShoppingList.LISTS).child(idList).child(ShoppingList.PRODUCTS)
                                .child(model.getId()).setValue(model);
                    }
                });
            }
        };

        listRecyclerView.setAdapter(firebaseAdapter);
        FloatingActionButton floatingButton = findViewById(R.id.listFloatingButton);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View mView = LayoutInflater.from(ProductActivity.this).inflate(R.layout.new_list_dialog, null);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProductActivity.this);
                alertDialog.setView(mView);

                final EditText textDialog = mView.findViewById(R.id.nameListDialog);
                TextView textView = mView.findViewById(R.id.dialogTitle);
                textView.setText(R.string.new_product);
                textDialog.setHint(R.string.product_name);
                alertDialog.setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                String nameList = textDialog.getText().toString().trim();
                                if (nameList.length() > 0) {
                                    ShoppingList shoppingList = new ShoppingList(nameList, false);
                                    databaseReference.child(ShoppingList.LISTS).child(idList).child(ShoppingList.PRODUCTS).push().setValue(shoppingList);
                                } else {
                                    Toast.makeText(ProductActivity.this, R.string.product_must_have_name, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_products:
                databaseReference.child(ShoppingList.LISTS).child(idList).child(ShoppingList.PRODUCTS).orderByChild(ShoppingList.BOUGHT)
                        .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                            postsnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            case R.id.remove_list:
                AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
                builder.setTitle(R.string.confirmation)
                        .setMessage(R.string.ask_remove_list)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                databaseReference.child(ShoppingList.LISTS).child(idList).removeValue();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        CheckBox listCheckBox;

        ListViewHolder(View v) {
            super(v);
            listCheckBox = itemView.findViewById(R.id.checkProduct);
        }
    }
}
