package com.example.chatfinal;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatfinal.Adapter.MyAdapter;
import com.example.chatfinal.Model.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    MyAdapter myAdapter;
    ArrayList<User> list= new ArrayList<>();

    ImageView userimg, uplimg,signout;
    TextView name;
    ProgressDialog progressDialog;
    Uri uri;
    ImageView logout;
    int flag=-1;

    // StorageReference storageReference;
    FirebaseDatabase database;
    StorageReference storageReference;
    FirebaseAuth auth;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        userimg  = findViewById(R.id.userimg);
        uplimg= findViewById(R.id.uploadimg);
        signout = findViewById(R.id.logOut);
        name =findViewById(R.id.textView3);



        recyclerView = findViewById(R.id.charrecv);
        databaseReference = FirebaseDatabase.getInstance().getReference("User");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Uploading Profile image");
        progressDialog.setMessage("Image uploading");


        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call the signOut method to log out the user
                FirebaseAuth.getInstance().signOut();

                // Create an Intent to navigate to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                // Clear all previous activities and start the LoginActivity as a new task
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Finish the current activity to prevent the user from coming back after logout
            }
        });



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    if (dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){}

                    else{
                        User user = new User();
                        user.setName(dataSnapshot.child("name").getValue(String.class));
                        user.setProfile_photo(dataSnapshot.child("profile_photo").getValue(String.class));
                        user.setUserID(dataSnapshot.getKey());

                        list.add(user);
                    }
                }
                MyAdapter myAdapter = new MyAdapter(getApplicationContext(),list);
                recyclerView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });















        database.getReference().child("User").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    User user = snapshot.getValue(User.class);
                    Picasso.get()
                            .load(user.getProfile_photo())
                            .placeholder(R.drawable.profile)
                            .into(userimg);
                    name.setText(user.getName());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });










        uplimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(MainActivity.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080,1080)
                        .createIntent(intent -> {
                            startForMediaPickerResult.launch(intent);
                            return null;
                        });

            }
        });


    }
    private final ActivityResultLauncher<Intent> startForMediaPickerResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                Intent data = result.getData();
                if (data != null && result.getResultCode() == Activity.RESULT_OK) {
                    uri = data.getData();

                    if (uri==null)
                    {
                        Toast.makeText(MainActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        progressDialog.show();

                        userimg.setImageURI(uri);

                        StorageReference dref = storageReference.child("profile_photo").child(FirebaseAuth.getInstance().getUid());
                        dref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();

                                Toast.makeText(MainActivity.this, "profile photo Updated", Toast.LENGTH_SHORT).show();
                                dref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        database.getReference().child("User").child(FirebaseAuth.getInstance().getUid()).child("profile_photo").setValue(uri.toString());


                                    }
                                });

                            }
                        });


                    }
                }
            }
    );




}