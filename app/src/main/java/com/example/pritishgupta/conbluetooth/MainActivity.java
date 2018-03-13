package com.example.pritishgupta.conbluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Button Create,login;
    private FirebaseAuth mAuth;
    private EditText email_reg,pswrd_reg;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth= FirebaseAuth.getInstance();
        email_reg=findViewById(R.id.editText);
        pswrd_reg=findViewById(R.id.editText1);
        Create=findViewById(R.id.button2);
        login=findViewById(R.id.button3);

        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=email_reg.getText().toString();
                String pswrd=pswrd_reg.getText().toString();
                progressBar = new ProgressDialog(view.getContext());
                progressBar.setCancelable(false);
                progressBar.setTitle("Please wait");
                progressBar.setMessage("Signing up");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.show();

                mAuth.createUserWithEmailAndPassword(email,pswrd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {   FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(MainActivity.this,"registered",Toast.LENGTH_LONG).show();
                            progressBar.dismiss();
                        }
                        else
                        {
                            String err=task.getException().getMessage();
                            Toast.makeText(MainActivity.this,err,Toast.LENGTH_LONG).show();
                            progressBar.dismiss();
                        }
                    }
                });


            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=email_reg.getText().toString();
                String pswrd=pswrd_reg.getText().toString();
                progressBar = new ProgressDialog(view.getContext());
                progressBar.setCancelable(false);
                progressBar.setTitle("Please wait");
                progressBar.setMessage("logging in ");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.show();


                mAuth.signInWithEmailAndPassword(email,pswrd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {   FirebaseUser user = mAuth.getCurrentUser();
                            progressBar.dismiss();
                            Intent i=new Intent(MainActivity.this,Connection.class);
                            startActivity(i);
                            finish();

                        }
                        else
                        {
                            String err=task.getException().getMessage();
                            Toast.makeText(MainActivity.this,err,Toast.LENGTH_LONG).show();
                            progressBar.dismiss();
                        }
                    }
                });
            }
        });

    }
    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser fuser=mAuth.getCurrentUser();
        if(fuser!=null)
        {
            Intent i=new Intent(MainActivity.this,Connection.class);
            startActivity(i);
            finish();
        }
    }

}

