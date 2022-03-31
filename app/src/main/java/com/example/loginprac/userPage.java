package com.example.loginprac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
//import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class AESUtils
{

    private static final byte[] keyValue =
            new byte[]{'c', 'o', 'd', 'i', 'n', 'g', 'a', 'f', 'f', 'a', 'i', 'r', 's', 'c', 'o', 'm'};


    public static String encrypt(String cleartext)
            throws Exception {
        byte[] rawKey = getRawKey();
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    public static String decrypt(String encrypted)
            throws Exception {

        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(enc);
        return new String(result);
    }

    private static byte[] getRawKey() throws Exception {
        SecretKey key = new SecretKeySpec(keyValue, "AES");
        byte[] raw = key.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKey skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] encrypted)
            throws Exception {
        SecretKey skeySpec = new SecretKeySpec(keyValue, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (byte b : buf) {
            appendHex(result, b);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}

class RSA {
    static String encrypt(String message, PublicKey pbKey) throws Exception {
        byte[] messageToByte= message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        //Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, pbKey);

        //Perform Encryption
        byte[] cipherText = cipher.doFinal(messageToByte);

        return tostring(cipherText);
    }
    private static String tostring(byte[] encoded){
        return Base64.getEncoder().encodeToString(encoded);
    }
    static String decrypt(String enctypedMessage, PrivateKey ptKey) throws Exception{
        byte[] encrypedBytes = Base64.getDecoder().decode(enctypedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE,ptKey);
        byte[] decryptedBytes = cipher.doFinal(encrypedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

public class userPage extends AppCompatActivity {
    FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference dRef;
    private Button logOutButton,sendButton;
    private TextView welcome;
    private EditText editText;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("messages");
    private String userName,userEmail;
    PrivateKey pvKey;
    PublicKey currPbKey;
    private EditText sendToEmail;
    CheckBox sendAll;

    public void sendToAll(){
        if(editText.length()>0){
            dRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<User> data = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot :snapshot.getChildren()) {
                            User element = dataSnapshot.getValue(User.class);
                            if(!element.isMaster) {
                                data.add(element);
                            }
                        }
                        String textMessage=editText.getText().toString();
                        boolean sendSelf=false;
                        for (User user: data){
                            PublicKey pbKey = null;
                            byte[] publicBytes = Base64.getMimeDecoder().decode(user.publicKey);
                            EncodedKeySpec  KeySpec = new X509EncodedKeySpec( publicBytes);
                            KeyFactory keyFactory = null;
                            try {
                                keyFactory = KeyFactory.getInstance("RSA");
                                assert keyFactory != null;
                                pbKey = keyFactory.generatePublic(KeySpec);
                            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                e.printStackTrace();
                            }
                            String encrypted = "",encrypted2="";
                            String sourceStr = "Master Sent: "+textMessage;
                            sourceStr = userName+"%%"+userEmail+"!!"+sourceStr;

                            Log.i("typed message",sourceStr);
                            try {
                                if (pbKey == null) {
                                    Log.i("pbkey", "isnull");
                                }
                                // Add here***********************************************************************
                                encrypted = RSA.encrypt(sourceStr, pbKey);
                                encrypted = user.email + "!!" + encrypted;

                                if(!sendSelf){
                                    sendSelf=true;
                                    encrypted2 = RSA.encrypt(sourceStr, currPbKey);
                                    encrypted2 = userEmail + "!!" + encrypted2;
                                }
                                // Add here***********************************************************************
                                myRef.push().setValue(encrypted);
                                myRef.push().setValue(encrypted2);
                                editText.setText("");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    public void sendToOne(){
        String email = sendToEmail.getText().toString().trim();
        if(email.isEmpty()){
            sendToEmail.setError("email is required");
            sendToEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            sendToEmail.setError("Provide valid email");
            sendToEmail.requestFocus();
            return;
        }

        if(editText.length()>0){
            Query checkUser = dRef.orderByChild("email").equalTo(email);
            checkUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    List<User> data = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot :snapshot.getChildren()) {
                            User element = dataSnapshot.getValue(User.class);
                            data.add(element);
                        }
                        PublicKey pbKey = null;
                        for (User user: data){

                            byte[] publicBytes = Base64.getMimeDecoder().decode(user.publicKey);
                            EncodedKeySpec  KeySpec = new X509EncodedKeySpec( publicBytes);
                            KeyFactory keyFactory = null;
                            try {
                                keyFactory = KeyFactory.getInstance("RSA");
                                assert keyFactory != null;
                                pbKey = keyFactory.generatePublic(KeySpec);
                            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                e.printStackTrace();
                            }
                        }
                        String encrypted = "",encrypted2="";
                        String sourceStr = "-> "+editText.getText().toString();
                        sourceStr = userName+"%%"+userEmail+"!!"+sourceStr;

                        Log.i("typed message",sourceStr);
                        try {
                            if(pbKey==null){
                                Log.i("pbkey","isnull");
                            }
                            // Add here***********************************************************************
                            Log.i("test1","test1");
                            encrypted = RSA.encrypt(sourceStr,pbKey);
                            Log.i("test2","test2");
                            encrypted2=RSA.encrypt(sourceStr,currPbKey);
                            Log.i("test3","test3");
                            encrypted=email+"!!"+encrypted;
                            encrypted2=userEmail+"!!"+encrypted2;
                            Log.i("test4","test4");
                            // Add here***********************************************************************
                            myRef.push().setValue(encrypted);
                            myRef.push().setValue(encrypted2);
                            Log.i("test5","test5");
                            editText.setText("");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.logOut:
                mAuth.signOut();
                startActivity(
                        new Intent(this,MainActivity.class)
                );
            case R.id.sendButton:
                if(sendAll.isChecked()){
                    sendToAll();
                }else{
                    sendToOne();
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        Log.i("check","after sed content view");
        editText = findViewById(R.id.sendMessage);
        ListView listView= findViewById(R.id.listView);

        sendToEmail = findViewById(R.id.sendEmail);

        mAuth=FirebaseAuth.getInstance();
        logOutButton=findViewById(R.id.logOut);
        logOutButton.setOnClickListener(this::onClick);

        sendButton=findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this::onClick);

        welcome=findViewById(R.id.welcomeMessage);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        Log.i("check","before sendAll");
        sendAll = findViewById(R.id.sendSlaves);

        assert user != null;
        userId=user.getUid();
        dRef = FirebaseDatabase.getInstance().getReference("Users");
        Log.i("check","gone to user page initialized values");
        dRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile = snapshot.getValue(User.class);
                Log.i("check","found current user");
                if(userProfile!=null){
                    if(userProfile.isMaster){
                        sendAll.setVisibility(View.VISIBLE);
                    }
                    userName=userProfile.fullName;
                    userEmail=userProfile.email;
                    //Get private key
                    byte[] privateBytes = Base64.getMimeDecoder().decode(userProfile.privateKey);
                    EncodedKeySpec  KeySpec = new PKCS8EncodedKeySpec( privateBytes);
                    KeyFactory keyFactory = null;
                    try {
                        keyFactory = KeyFactory.getInstance("RSA");
                        assert keyFactory != null;
                        pvKey = keyFactory.generatePrivate(KeySpec);
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                    // get public key
                    byte[] publicBytes = Base64.getMimeDecoder().decode(userProfile.publicKey);
                      KeySpec = new X509EncodedKeySpec( publicBytes);
                     keyFactory = null;
                    try {
                        keyFactory = KeyFactory.getInstance("RSA");
                        assert keyFactory != null;
                        currPbKey = keyFactory.generatePublic(KeySpec);
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }

                    String message="Welcome "+userProfile.fullName+",";
                    welcome.setText(message);
                }else{
                    Toast.makeText(userPage.this, "no user found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(userPage.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                if(dataSnapshot.exists()){
                    String value = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    value = value.substring(1, value.length() - 1);
                    String[] separated = value.split(", ");
                    Arrays.sort(separated);
                    ArrayList<String> arr = new ArrayList<>();
                    for (String ch : separated) {
                        String[] mes = ch.split("=", 2);
                        String encrypted = mes[1];
                        String decrypted = "";
                        try {

                            String[] breakMessage= encrypted.split("!!",2);

                            if(breakMessage[0].equals(userEmail)) {
                                // Add here***********************************************************************
                                decrypted = RSA.decrypt(breakMessage[1],pvKey);
                                // Add here***********************************************************************
                                String[] senderMess=decrypted.split("!!",2);
                                String[] senderInfo = senderMess[0].split("%%",2);
                                decrypted=senderInfo[0]+"("+senderInfo[1]+"):";
                                arr.add(decrypted);
                                arr.add(senderMess[1]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(userPage.this, android.R.layout.simple_list_item_1, arr);
                    listView.setAdapter(arrayAdapter);
                }else{
                    Toast.makeText(userPage.this, "Start Texting", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Toast.makeText(userPage.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


    }

}
