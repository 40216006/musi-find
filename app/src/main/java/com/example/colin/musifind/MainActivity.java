package com.example.colin.musifind;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private arrayAdapter arrayAdapter;
    private int i;

    private FirebaseAuth mAuth;

    private String currentUid;

    private DatabaseReference usersDb;

    ListView listView;
    List<cards> rowItems;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUid = mAuth.getCurrentUser().getUid();

        checkUserSex();


        mAuth = FirebaseAuth.getInstance();

        rowItems = new ArrayList<cards>();


        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems );

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject

                cards obj = (cards) dataObject;
                String userId = obj.getUserid();
                usersDb.child(oppositeUserSex).child(userId).child("connections").child("nope").child(currentUid).setValue(true);

               Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRightCardExit(Object dataObject) {

                cards obj = (cards) dataObject;
                String userId = obj.getUserid();
                usersDb.child(oppositeUserSex).child(userId).child("connections").child("yep").child(currentUid).setValue(true);

                isConnectionMatch(userId);

                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(userSex).child(currentUid).child("connections").child("yep").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "new connection", Toast.LENGTH_LONG).show();
                    usersDb.child(oppositeUserSex).child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUid).setValue(true);
                    usersDb.child(userSex).child(currentUid).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private String userSex;
    public String oppositeUserSex;
    public void checkUserSex(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference maleDb = FirebaseDatabase.getInstance().getReference().child("users").child("Male");
        maleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(user.getUid()))
                    userSex = "Male";
                    oppositeUserSex = "Female";
                    getOppositeSexUsers();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DatabaseReference femaleDb = FirebaseDatabase.getInstance().getReference().child("users").child("Female");
        femaleDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(user.getUid()))
                    userSex = "Female";
                oppositeUserSex = "Male";
                getOppositeSexUsers();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getOppositeSexUsers(){
        DatabaseReference oppositeSexDb = FirebaseDatabase.getInstance().getReference().child("users").child(oppositeUserSex);
        oppositeSexDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
              if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUid)&& !dataSnapshot.child("connections").child("yep").hasChild(currentUid)){
                  cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString());
                  rowItems.add(item);
                  arrayAdapter.notifyDataSetChanged();
              }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginOrRegisterActivity.class);
        startActivity(intent);
        finish();
        return;
    }


    public void goToSettings(View view){
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.putExtra("userSex", userSex);
        startActivity(intent);

        return;

    }

}