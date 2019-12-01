package com.nullexcom.find;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.edtEmail)
    EditText edtEmail;

    @BindView(R.id.edtName)
    EditText edtName;

    @BindView(R.id.edtPassword)
    EditText edtPassword;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        loadingDialog = new LoadingDialog(this);
    }

    @OnClick(R.id.imgBack)
    void back() {
        super.onBackPressed();
    }

    @OnClick(R.id.tvSignUp)
    void signUp() {
        String email = edtEmail.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email is not valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidName(name)) {
            Toast.makeText(this, "Name can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                            builder.setDisplayName(name).build();
                            if (user == null) {
                                return;
                            }
                            user.updateProfile(builder.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadingDialog.cancel();
                                    toGPS();
                                }
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            loadingDialog.cancel();
                        }
                    }
                });

    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private boolean isValidName(String name) {
        return name != null && !name.isEmpty();
    }

    private void toGPS() {
        Intent intent = new Intent(this, GPSActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
