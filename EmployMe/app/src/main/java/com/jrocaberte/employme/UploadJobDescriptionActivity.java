package com.jrocaberte.employme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class UploadJobDescriptionActivity extends AppCompatActivity {

    private Button mUploadJobDescription, mSubmitJobDescription;

    private Uri resultUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId;

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_job_description);

        mUploadJobDescription = (Button) findViewById(R.id.uploadJobDescription);
        mSubmitJobDescription = (Button) findViewById(R.id.submitJobDescription);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mUploadJobDescription.setOnClickListener(new View.OnClickListener() {
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
                intent.setType("application/pdf");

                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        mSubmitJobDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveJobDescription();
            }
        });

    }

    private void saveJobDescription() {
        if(resultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("jobDescriptions").child(userId);
            Log.d("ResultURI", resultUri.toString());

            UploadTask uploadTask = filepath.putFile(resultUri);
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
                    userInfo.put("jobDescriptionUrl", downloadUrl.toString());
                    mUserDatabase.updateChildren(userInfo);

                    Intent intent = new Intent(UploadJobDescriptionActivity.this, MainActivity.class);
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
            mUploadJobDescription.setText("Upload Complete");
        }
    }

}
