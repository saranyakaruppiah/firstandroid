package com.example.letgift;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.util.Arrays;

public class RegistrationActivity extends AppCompatActivity {
    EditText user_name,password,confirmpassword,mobile,email;
    Button signup;
    RadioGroup radioGroup;
    TextView already_reg;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ImageView google, facebook, twitter, linkedin;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 0;
    String access_token,login_type;


    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    CallbackManager mCallbackManager;
    private TwitterAuthClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        user_name = findViewById(R.id.user_name);
        email = findViewById(R.id.eamil);
        mobile = findViewById(R.id.mobile);
        confirmpassword = findViewById(R.id.confirm);
        password = findViewById(R.id.Passwords);
        signup = findViewById(R.id.signup);
        radioGroup = findViewById(R.id.radio_group);
        already_reg = findViewById(R.id.already_reg);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);
        twitter = findViewById(R.id.twitter);
        linkedin = findViewById(R.id.linkedin);
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        mAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_name.getText() != null && user_name.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "User name is empty", Toast.LENGTH_SHORT).show();

                } else if (email.getText() != null && email.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Email is empty", Toast.LENGTH_SHORT).show();
                } else if (!email.getText().toString().trim().matches(emailPattern)) {
                    Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                } else if (mobile.getText() != null && mobile.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Mobile Number is empty", Toast.LENGTH_SHORT).show();
                } else if (mobile.getText().toString().length() < 10) {
                    Toast.makeText(getApplicationContext(), "Invalid mobile number", Toast.LENGTH_SHORT).show();
                } else if (password.getText() != null && password.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_SHORT).show();
                } else if (confirmpassword.getText() != null && confirmpassword.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Confirm Password is empty", Toast.LENGTH_SHORT).show();
                } else if (!password.getText().toString().equals(confirmpassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Password is mismatch", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sh1 = getSharedPreferences("letgift", Context.MODE_PRIVATE);
                    SharedPreferences.Editor shared_edit1=sh1.edit();
                    shared_edit1.putString("user_name",user_name.getText().toString());
                    shared_edit1.putString("password",password.getText().toString());

                    shared_edit1.commit();
                    Toast.makeText(getApplicationContext(), "Registration Successfully", Toast.LENGTH_SHORT).show();

                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    Toast.makeText(RegistrationActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
                }

            }
        });


        already_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                finish();

            }
        });


        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                login_type="google";
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(RegistrationActivity.this, gso);
                signIn();

            }
        });
//                startActivity(new Intent(RegistrationActivity.this,AddFriendActivity.class));
        mCallbackManager = CallbackManager.Factory.create();

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_type="Facebook";
                FirebaseAuth.getInstance().signOut();

//                LoginManager.getInstance().logInWithPublishPermissions(
//                        LoginActivity.this,
//                        Arrays.asList("publish_actions"));
//                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile", "user_friends"));
                //AccessToken.getCurrentAccessToken().getPermissions();
                LoginManager.getInstance().logInWithReadPermissions(RegistrationActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
//
                        Log.d(TAG, " Access Token Login Rs " + loginResult.getAccessToken().getToken());
                        Log.d(TAG, "USER TOKEN " + loginResult.getAccessToken().getDeclinedPermissions());
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        // ...
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        // ...
                    }
                });

            }
        });
        client = new TwitterAuthClient();

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.authorize(RegistrationActivity.this, new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> twitterSessionResult) {
                        Toast.makeText(RegistrationActivity.this, "Signed in to twitter successful", Toast.LENGTH_LONG).show();
                        signInToFirebaseWithTwitterSession(twitterSessionResult.data);
//                mTwitterBtn.setVisibility(View.VISIBLE);
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Toast.makeText(RegistrationActivity.this, "Login failed. No internet or No Twitter app found on your phone", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("requestCode", String.valueOf(requestCode));
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.w(TAG, "gmail Login");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        else
        {
            Log.w(TAG, "facebook Login");
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        Log.d(TAG,"Token No"+acct.getIdToken());
        access_token= acct.getIdToken();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(),"This email is already used.Please login different mail id",Toast.LENGTH_SHORT).show();
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                    }
                });
    }
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            // Name, email address, and profile photo Url
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();
            String image=photoUrl.toString();
//            Profile.getCurrentProfile();
            Log.e("social network  detail ",name +" "+ email +" "+ image+" "+currentUser.getPhoneNumber()); //google social network  detail: Saranya Arumugam saranya.rayaz@gmail.com https://lh4.googleusercontent.com/-POt1IkDNMbI/AAAAAAAAAAI/AAAAAAAAAAA/AMZuucnyGEKIUq_J7I92ZnfPcrovY7JT-Q/s96-c/photo.jpg null
            //facebook social network  detail: Saranya Sixface asaranyamdu@gmail.com https://graph.facebook.com/2868768993405637/picture null

            boolean emailVerified = currentUser.isEmailVerified();
            String uid = currentUser.getUid();
            Log.d("UserID", "USERID " + uid);
            if(email==null){
//            Log.d(TAG,Profile.getCurrentProfile().get)
            }
            if(login_type=="google")
            {
                Toast.makeText(RegistrationActivity.this, "Signed in to google successful from "+name, Toast.LENGTH_LONG).show();

            }
            else
            {
                Toast.makeText(RegistrationActivity.this, "Signed in to facebook successful from "+name, Toast.LENGTH_LONG).show();

            }

//            social_login(name,email,image,uid,access_token);


        }


    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        access_token= token.getToken();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.d(TAG, "handleFacebookAccessToken:" + token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Log.d(TAG,""+task.getResult());
                            FirebaseUser user = task.getResult().getUser();
                            Log.d(TAG,"User "+user.getEmail());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
    private void signInToFirebaseWithTwitterSession(TwitterSession session){
        Log.d("twitter session", String.valueOf(session));

        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("twitter", String.valueOf(task));
                        TwitterCore.getInstance().getApiClient().getAccountService().verifyCredentials(true, true, false).enqueue(new Callback<User>() {
                            @Override
                            public void success(Result<User> result) {
                                User datas = result.data;

                                Log.d("profileImage", String.valueOf(datas));
                                Log.d("name", String.valueOf(datas.name));
                                Toast.makeText(RegistrationActivity.this, "Signed in firebase twitter successful from "+datas.name, Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void failure(TwitterException exception) {

                            }
                        });

                        if (!task.isSuccessful()){
                            Toast.makeText(RegistrationActivity.this, "Auth firebase twitter failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}