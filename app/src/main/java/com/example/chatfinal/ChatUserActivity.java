package com.example.chatfinal;
import com.github.dhaval2404.imagepicker.ImagePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatfinal.Adapter.MessageAdapter;
import com.example.chatfinal.Model.MessagesModel;
import com.example.chatfinal.Model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatUserActivity extends AppCompatActivity {
    FirebaseAuth auth;
    TextView name;
    EditText etmesage;

    MessageAdapter messageAdapter;
    ArrayList<MessagesModel> list=new ArrayList<>();
    ImageView pic,back,sendmsg;
    RecyclerView chat;
    ImageView sendImage; // Add ImageView for sending images

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user);

        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        name =findViewById(R.id.sendername);
        pic= findViewById(R.id.senderpic);
        sendmsg = findViewById(R.id.send);
        chat =findViewById(R.id.chat);
        etmesage = findViewById(R.id.etmessage);
        back = findViewById(R.id.baclk);
        sendImage = findViewById(R.id.sendImage);
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the image picker when the "send image" button is clicked
                ImagePicker.with(ChatUserActivity.this)
                        .galleryOnly()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });
        final  String senderID= auth.getUid();  // making a variable make it global even inside declare inside any function
        String receverid= getIntent().getStringExtra("userId");

        FirebaseDatabase.getInstance().getReference()
                .child("User")
                .child(receverid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        Picasso.get()
                                .load(user.getProfile_photo())
                                .placeholder(R.drawable.profile)
                                .into(pic);
                        name.setText(user.getName());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatUserActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


//final ArrayList<MessagesModel> messagesModels = new ArrayList<>();
//final MessageAdapter messageAdapter = new MessageAdapter(messagesModels,this);

//chat.setAdapter(messageAdapter);

        // chat.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chat.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(list,this);
        chat.setAdapter(messageAdapter);
        final String senderroom = senderID +receverid;
        final String recieverroom = receverid + senderID;



        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderroom)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot:snapshot.getChildren())
                        {
                            MessagesModel model = dataSnapshot.getValue(MessagesModel.class);
                            list.add(model);

                        }

                        messageAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });






        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message= etmesage.getText().toString();
                final MessagesModel model = new MessagesModel(senderID,message); // we created a contructor for this purpose
                model.setTimestamp(new Date().getTime());
                etmesage.setText("");



                FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(senderroom)
                        .push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {


                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(recieverroom)
                                        .push()
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });




                            }
                        });






            }
        });






    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle the image selected by the user
        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // Upload the image to Firebase Storage and send it in the chat
                sendImageMessage(uri);
            }
        }
    }

    private void sendImageMessage(Uri imageUri) {
        String senderID = auth.getUid();
        String receiverID = getIntent().getStringExtra("userId");
        final String senderRoom = senderID + receiverID;
        final String receiverRoom = receiverID + senderID;

        final MessagesModel imageModel = new MessagesModel(senderID, "", imageUri.toString());
        imageModel.setType("image");
        imageModel.setTimestamp(new Date().getTime());

        // Upload the image to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final String imageName = "image_" + System.currentTimeMillis();
        StorageReference imageRef = storageReference.child("chats").child(senderRoom).child(imageName);
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Update the image message with the image URL
                                imageModel.setMessage(uri.toString());

                                // Save the image message to sender and receiver chat rooms
                                FirebaseDatabase.getInstance().getReference().child("chats")
                                        .child(senderRoom).push().setValue(imageModel);
                                FirebaseDatabase.getInstance().getReference().child("chats")
                                        .child(receiverRoom).push().setValue(imageModel);

                                // Notify the adapter to update the chat
                                list.add(imageModel);
                                messageAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }
}
