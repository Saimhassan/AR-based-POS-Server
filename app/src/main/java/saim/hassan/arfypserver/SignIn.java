package saim.hassan.arfypserver;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import saim.hassan.arfypserver.Model.User;

public class SignIn extends AppCompatActivity {

    EditText edtPhone,edtPassword;
    Button btnSignIn;

    FirebaseDatabase db;
    DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = (MaterialEditText)findViewById(R.id.txtpassword);
        edtPhone = (MaterialEditText)findViewById(R.id.txtphone);

        btnSignIn = (Button)findViewById(R.id.sign_in);

        //Init Firebase
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser(edtPhone.getText().toString(),edtPassword.getText().toString());
            }
        });

    }
    private void signInUser(final String phone, String password) {

        final ProgressDialog mProgressDialog = new ProgressDialog(SignIn.this);
        mProgressDialog.setMessage("Please Wait");
        mProgressDialog.show();

        final String localPhone = phone;
        final String localPassword = password;

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localPhone).exists())
                {
                    mProgressDialog.dismiss();
                    User user = dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);
                    if (Boolean.parseBoolean(user.getIsStaff())) {

                            if (user.getPassowrd().equals(localPassword)) {
                            //Login Ok
                        }
                        else
                        {
                            Toast.makeText(SignIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                        Toast.makeText(SignIn.this, "Please Login with the Staff Account", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mProgressDialog.dismiss();
                    Toast.makeText(SignIn.this, "User not Exists in Database", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
