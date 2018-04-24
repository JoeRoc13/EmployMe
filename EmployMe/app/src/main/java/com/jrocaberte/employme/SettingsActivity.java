package com.jrocaberte.employme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField;

    private Button mViewResume, mCloseResume, mUploadResume, mViewJobDescription, mCloseJobDescription, mUploadJobDescription, mConfirm;

    private ImageView mProfileImage;

    private LinearLayout mApplicantLayout, mEmployerLayout;

    private PDFView pdfView;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, name, phone, profileImageUrl, profileResumeUrl, userType;

    private Uri resultUri, resultUriResume;

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupActionBar();

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mViewResume = (Button) findViewById(R.id.viewResume);
        mCloseResume = (Button) findViewById(R.id.closeResume);
        mUploadResume = (Button) findViewById(R.id.uploadResume);

        mViewJobDescription = (Button) findViewById(R.id.viewJobDescription);
        mCloseJobDescription = (Button) findViewById(R.id.closeJobDescription);
        mUploadJobDescription = (Button) findViewById(R.id.uploadJobDescription);

        mConfirm = (Button) findViewById(R.id.confirm);

        mApplicantLayout = (LinearLayout) findViewById(R.id.applicantLayout);
        mEmployerLayout = (LinearLayout) findViewById(R.id.employerLayout);

        pdfView = (PDFView) findViewById(R.id.pdfView);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
                intent.setType("image/*");

                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        mViewResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserDatabase.child("profileResumeUrl").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mUploadResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, UploadResumeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mViewJobDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserDatabase.child("jobDescriptionUrl").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mUploadJobDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, UploadJobDescriptionActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
    }

    private void setupActionBar() {
        getSupportActionBar().setTitle("Settings");
    }


    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null) {
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("phone") != null) {
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("type") != null) {
                        userType = map.get("type").toString();
                        if(userType.equals("Applicant")) {
                            mApplicantLayout.setVisibility(View.VISIBLE);
                        } else if(userType.equals("Employer")) {
                            mEmployerLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    Glide.clear(mProfileImage);
                    if(map.get("profileImageUrl") != null) {
                        profileImageUrl = map.get("profileImageUrl").toString();
                        switch (profileImageUrl) {
                            case "default":
                                mProfileImage.setImageResource(R.drawable.default_profile);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).centerCrop().into(mProfileImage);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void saveUserInformation() {
        name = mNameField.getText().toString();
        phone = PhoneNumberUtils.formatNumber(mPhoneField.getText().toString());

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        mUserDatabase.updateChildren(userInfo);

        if(resultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map userInfo = new HashMap();
                    userInfo.put("profileImageUrl", downloadUrl.toString());
                    mUserDatabase.updateChildren(userInfo);

                    finish();
                    return;
                }
            });
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
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