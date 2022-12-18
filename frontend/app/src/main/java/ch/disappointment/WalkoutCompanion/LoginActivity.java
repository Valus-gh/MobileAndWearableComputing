package ch.disappointment.WalkoutCompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.api.model.CredentialsDto;
import ch.disappointment.WalkoutCompanion.persistence.TokensDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.User;

/**
 * Activity that handles the login, it's the first activity that is started
 */
public class LoginActivity extends AppCompatActivity {
    private EditText inputPassword;
    private EditText inputUsername;
    private TokensDaoService tokensDaoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Fetch the last user, and if there is one, check if the token is still valid
        // if it is, start the main activity with the logged user
        tokensDaoService = new TokensDaoService(this);
        tokensDaoService.getLastUserExcept(this, "LOCAL_USER", user -> {
            if (user != null) {
                ApiService apiService = ApiService.getInstance(this);
                apiService.setUser(user);
                apiService.setLocal(false);

                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                startActivity(mainActivityIntent);
            }
        });


        inputPassword = findViewById(R.id.passwordInput);
        inputUsername = findViewById(R.id.emailInput);

        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button localLoginButton = findViewById(R.id.useLocalButton);

        // set a click listener that starts the main activity after the user has logged in
        localLoginButton.setOnClickListener((view -> {
            ApiService apiService = ApiService.getInstance(this);
            apiService.setLocal(true);
            apiService.setUser(new User("LOCAL_USER", null));

            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
        }));

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

        // Logins, save the user token and start the main activity
        ApiService.getInstance(ctx).login(credentialsDto, (tk) -> {
            Log.i("LOGIN_ACTIVITY", "Logged in user with token " + tk.getAccessToken());
            TokensDaoService service = new TokensDaoService(ctx);
            service.setToken(ctx, new User(credentialsDto.getUsername(), tk.getAccessToken()), () -> {
            });

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
            TokensDaoService service = new TokensDaoService(ctx);
            service.setToken(ctx, new User(credentialsDto.getUsername(), tk.getAccessToken()), () -> {
            });
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