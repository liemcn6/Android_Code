package com.example.app_like_amazon.Buyers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_like_amazon.Admin.AdminCategoryActivity;
import com.example.app_like_amazon.Model.Users;
import com.example.app_like_amazon.Prevalent.Prevalent;
import com.example.app_like_amazon.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    private EditText InputPhoneNumber, InputPassword;
    private Button LoginButton;
    private TextView ForgetPasswordLink;
    private CheckBox checkBox;
    private DatabaseReference RootRef;
    private String parentDbName = "Users";
    private TextView AdminLink, NotAdminLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        intialization();

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequesrLogin();
            }
        });

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginButton.setText("Admin Login");
                NotAdminLink.setVisibility(View.VISIBLE);
                AdminLink.setVisibility(View.INVISIBLE);
                parentDbName = "Admins";
                checkBox.setVisibility(View.INVISIBLE);
            }
        });

        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setVisibility(View.VISIBLE);
                LoginButton.setText("Login");
                NotAdminLink.setVisibility(View.INVISIBLE);
                AdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Users";
            }
        });

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check","login");
                startActivity(intent);
            }
        });
    }

    private void RequesrLogin() {
        String phone = InputPhoneNumber.getText().toString();
        String password = InputPassword.getText().toString();

        if(TextUtils.isEmpty(phone)){
            Toast.makeText(LoginActivity.this, "Vui l??ng nh???p s??? ??i???n tho???i", Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "Vui l??ng nh???p m???t kh???u", Toast.LENGTH_SHORT).show();
            return;
        }else{
            SuccessAcount(phone,password);
        }
    }

    private void SuccessAcount(final String phone, final String password) {

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(parentDbName).child(phone).exists()){
                    Users userData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);
                    if(userData.getPhone().equals(phone) && userData.getPassword().equals(password)){

                        // N???u checkBox th?? ghi nh??? n???i dung ????ng nh???p
                        if(checkBox.isChecked()){
                            Paper.book().write(Prevalent.UserPhoneKey,phone);
                            Paper.book().write(Prevalent.UserPasswordKey,password);

                        }

                        // Ki???m tra xem l?? ng?????i d??ng hay Admins
                        if(parentDbName.equals("Admins")){
                            Toast.makeText(LoginActivity.this, "Admin Login...", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                            startActivity(intent);
                        }else if(parentDbName.equals("Users")){
                            Prevalent.currentOnlineUser = userData;
                            Toast.makeText(LoginActivity.this, "????ng nh???p th??nh c??ng...", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }

                    }else{
                        Toast.makeText(LoginActivity.this, "S??? ??i???n tho???i ho???c m???t kh???u kh??ng ????ng", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(LoginActivity.this, "????ng nh???p th???t b???i...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void intialization() {
        LoginButton         = findViewById(R.id.login_btn);
        InputPhoneNumber    = findViewById(R.id.login_phone_number_input);
        InputPassword       = findViewById(R.id.login_password_input);
        checkBox            = findViewById(R.id.remember_me_chkb);
        ForgetPasswordLink      = findViewById(R.id.forget_password_link);
        AdminLink           = findViewById(R.id.admin_panel_link);
        NotAdminLink        = findViewById(R.id.not_admin_panel_link);
        RootRef             = FirebaseDatabase.getInstance().getReference();
        Paper.init(this);


    }
}
