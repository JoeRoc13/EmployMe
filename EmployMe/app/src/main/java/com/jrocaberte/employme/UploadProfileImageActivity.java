package com.jrocaberte.employme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadProfileImageActivity extends AppCompatActivity {

    private Button mUploadProfileImage, mSubmitProfileImage;

    private Uri resultUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId;

    private String userType;

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_image);

        mUploadProfileImage = (Button) findViewById(R.id.uploadProfileImage);
        mSubmitProfileImage = (Button) findViewById(R.id.submitProfileImage);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        getUserType();

        mUploadProfileImage.setOnClickListener(new View.OnClickListener() {
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

        mSubmitProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfileImage();
            }
        });
    }

    private void getUserType() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("type").getValue() != null) {
                        userType = dataSnapshot.child("type").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveProfileImage() {
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

                    Intent oldIntent = getIntent();
                    Intent intent = null;
                    if(oldIntent.getExtras() != null) {
                        Bundle b = oldIntent.getExtras();
                        if(b.get("SignedUp").equals(true)) {
                            intent = new Intent(UploadProfileImageActivity.this, MainActivity.class);
                        }
                    } else {
                        if(userType.equals("Applicant")) {
                            intent = new Intent(UploadProfileImageActivity.this, UploadResumeActivity.class);
                        } else {
                            intent = new Intent(UploadProfileImageActivity.this, UploadJobDescriptionActivity.class);
                        }
                    }
                    startActivity(intent);
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
            mUploadProfileImage.setText("Upload Complete");
        }
    }
}
