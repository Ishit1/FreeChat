package com.example.loginprac;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class User {
    public String fullName,email,age,privateKey,publicKey;
    public boolean isMaster;
    public User(){

    }

    public User(String name, String email, String age, String publicK, String privateK,boolean isM) {
        this.fullName=name;
        this.email=email;
        this.age=age;
        this.privateKey=privateK;
        this.publicKey=publicK;
        this.isMaster=isM;
    }
}
