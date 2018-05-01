package com.jrocaberte.employme;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jrocaberte.employme.Cards.Cards;
import com.jrocaberte.employme.Cards.CustomArrayAdapter;
import com.jrocaberte.employme.Matches.MatchesActivity;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CustomArrayAdapter arrayAdapter;
    private SwipeFlingAdapterView flingContainer;
    private PDFView pdfView;
    private Button mCloseResume, mCloseJobDescription;

    private String userType;
    private String oppositeUserType;

    private FirebaseAuth mAuth;

    private String currentUid;

    private DatabaseReference usersDb;

    List<Cards> rowItems;

    private TextView mNoUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();

        checkUserType();

        rowItems = new ArrayList<Cards>();

        arrayAdapter = new CustomArrayAdapter(this, R.layout.item, rowItems);

        pdfView = (PDFView) findViewById(R.id.pdfView);

        mCloseResume = (Button) findViewById(R.id.closeResume);
        mCloseJobDescription = (Button) findViewById(R.id.closeJobDescription);

        mNoUsers = (TextView)findViewById(R.id.noUsers);

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setVisibility(View.VISIBLE);
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
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                if(!userId.equals("")) {
                    usersDb.child(userId).child("connections").child("swiped_left").child(currentUid).setValue(true);
                }
                checkIfNoUsersLeft();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                if(!userId.equals("")) {
                    usersDb.child(userId).child("connections").child("swiped_right").child(currentUid).setValue(true);
                    isConnectionMatch(userId);
                }
                checkIfNoUsersLeft();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, final Object dataObject) {
                Cards obj = (Cards) dataObject;
                String userId = obj.getUserId();
                if(oppositeUserType.equals("Applicant")) {
                    usersDb.child(userId).child("profileResumeUrl").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                String profileResumeUrl = dataSnapshot.getValue().toString();
                                new RetrievePDFStream().execute(profileResumeUrl);
                                pdfView.setVisibility(View.VISIBLE);
                                mCloseResume.setVisibility(View.VISIBLE);

                                mCloseResume.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        pdfView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    usersDb.child(userId).child("jobDescriptionUrl").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                String jobDescriptionUrl = dataSnapshot.getValue().toString();
                                new RetrievePDFStream().execute(jobDescriptionUrl);
                                pdfView.setVisibility(View.VISIBLE);
                                mCloseJobDescription.setVisibility(View.VISIBLE);

                                mCloseJobDescription.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        pdfView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }

    private void checkIfNoUsersLeft() {
        if(rowItems.size() == 0) {
            flingContainer.setVisibility(View.GONE);
            mNoUsers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUid).child("connections").child("swiped_right").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Toast.makeText(MainActivity.this, "New Connection!", Toast.LENGTH_LONG).show();
                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();
                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUid).child("ChatId").setValue(key);
                    usersDb.child(currentUid).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkUserType() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("type").getValue() != null) {
                        userType = dataSnapshot.child("type").getValue().toString();
                        switch (userType){
                            case "Applicant":
                                oppositeUserType = "Employer";
                                break;
                            case "Employer":
                                oppositeUserType = "Applicant";
                                break;
                        }
                        getOppositeTypeUsers();
                        checkFilesUploaded();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    private void checkFilesUploaded() {
        usersDb.child(currentUid).child("profileImageUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.getValue().equals("default")) {
                        Intent intent = new Intent(MainActivity.this, UploadProfileImageActivity.class);
                        intent.putExtra("SignedUp", true);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(userType.equals("Applicant")) {
            usersDb.child(currentUid).child("profileResumeUrl").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()) {
                        Intent intent = new Intent(MainActivity.this, UploadResumeActivity.class);
                        intent.putExtra("SignedUp", true);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            usersDb.child(currentUid).child("jobDescriptionUrl").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()) {
                        Intent intent = new Intent(MainActivity.this, UploadJobDescriptionActivity.class);
                        intent.putExtra("SignedUp", true);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void getOppositeTypeUsers() {
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("type").getValue() != null) {
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("swiped_left").hasChild(currentUid) && !dataSnapshot.child("connections").child("swiped_right").hasChild(currentUid) && dataSnapshot.child("type").getValue().toString().equals(oppositeUserType)) {
                        flingContainer.setVisibility(View.VISIBLE);
                        String profileImageUrl = "default";
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }
                        Cards item = new Cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                        checkIfNoUsersLeft();
                    } else {
                        checkIfNoUsersLeft();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
        return false;
    }

    class RetrievePDFStream extends AsyncTask<String, Void, InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                if(urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            pdfView.fromStream(inputStream).load();
        }
    }
}
