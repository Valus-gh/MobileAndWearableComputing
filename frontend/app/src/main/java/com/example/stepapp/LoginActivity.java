package com.example.stepapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.stepapp.api.ApiService;
import com.example.stepapp.api.model.CredentialsDto;
import com.example.stepapp.api.model.TokenDto;
import com.google.android.material.textfield.TextInputEditText;

import java.util.function.Consumer;

public class LoginActivity extends AppCompatActivity {
    private EditText inputPassword;
    private EditText inputUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputPassword = findViewById(R.id.passwordInput);
        inputUsername = findViewById(R.id.emailInput);

        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new LoginClickListener(this, inputUsername, inputPassword));
        registerButton.setOnClickListener(new RegisterClickListener(this, inputUsername, inputPassword));
    }
}


class LoginClickListener implements View.OnClickListener {
    private final Context ctx;
    private final EditText usernameInput;
    private final EditText passwordInput;

    LoginClickListener(Context ctx, EditText usernameInput, EditText passwordInput) {
        this.ctx = ctx;
        this.usernameInput = usernameInput;
        this.passwordInput = passwordInput;
    }

    @Override
    public void onClick(View view) {
        CredentialsDto credentialsDto = new CredentialsDto(
                usernameInput.getText().toString(),
                passwordInput.getText().toString()
        );

        ApiService.getInstance(ctx).login(credentialsDto, (tk) -> {
            Log.i("LOGIN_ACTIVITY", "Logged in user with token " + tk.getAccessToken());
            ApiService.getInstance(ctx).getGreet(greet -> {
                        Toast.makeText(ctx, greet, Toast.LENGTH_SHORT).show();
                        Toast.makeText(ctx, greet, Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(ctx, MainActivity.class);
                        ctx.startActivity(myIntent);
                    },
                    err -> {
                        Toast.makeText(ctx, "Error! Please restart the app", Toast.LENGTH_LONG).show();
                    });
        }, (err) -> {
            Log.e("LOGIN_ACTIVITY", "Error logging in: " + err.getMessage());
            err.printStackTrace();
            Toast.makeText(ctx, "Error! Wrong username or password", Toast.LENGTH_LONG).show();
        });
    }
}

class RegisterClickListener implements View.OnClickListener {
    private final Context ctx;
    private final EditText usernameInput;
    private final EditText passwordInput;

    RegisterClickListener(Context ctx, EditText usernameInput, EditText passwordInput) {
        this.ctx = ctx;
        this.usernameInput = usernameInput;
        this.passwordInput = passwordInput;
    }

    @Override
    public void onClick(View view) {
        CredentialsDto credentialsDto = new CredentialsDto(
                usernameInput.getText().toString(),
                passwordInput.getText().toString()
        );

        ApiService.getInstance(ctx).register(credentialsDto, (tk) -> {
            Log.i("LOGIN_ACTIVITY", "Registered user with token " + tk.getAccessToken());
            ApiService.getInstance(ctx).getGreet(greet -> {
                        Toast.makeText(ctx, greet, Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(ctx, MainActivity.class);
                        ctx.startActivity(myIntent);
                    },
                    err -> {
                        Toast.makeText(ctx, "Error! Please restart the app", Toast.LENGTH_LONG).show();
                    });
        }, (err) -> {
            Log.e("LOGIN_ACTIVITY", "Error logging in: " + err.getMessage());
            err.printStackTrace();
            Toast.makeText(ctx, "Error! Wrong username or password", Toast.LENGTH_LONG).show();
        });
    }
}