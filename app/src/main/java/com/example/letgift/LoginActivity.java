package com.example.letgift;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.TwitterAuthProvider;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    EditText user_name, password;
    Button signin, signup;
    ImageView google, facebook, twitter, linkedin;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 0;
    String access_token, login_type;


    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    CallbackManager mCallbackManager;
    private TwitterLoginButton mTwitterBtn;
    private TwitterAuthClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This code must be entering before the setContentView to make the twitter login work...
        TwitterAuthConfig mTwitterAuthConfig = new TwitterAuthConfig(getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));
        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(mTwitterAuthConfig)
                .build();
        Twitter.initialize(twitterConfig);

        setContentView(R.layout.activity_main);
        user_name = findViewById(R.id.user_name);
        password = findViewById(R.id.Passwords);
        signin = findViewById(R.id.signin);
        signup = findViewById(R.id.signup);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);
        twitter = findViewById(R.id.twitter);
        linkedin = findViewById(R.id.linkedin);
        mTwitterBtn = findViewById(R.id.twitter_login_button);
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        mAuth = FirebaseAuth.getInstance();
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);

        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.letgift",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_name.getText() != null && user_name.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "User name is empty", Toast.LENGTH_SHORT).show();

                } else if (password.getText() != null && password.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences prefs = getSharedPreferences("letgift", MODE_PRIVATE);
                    String username = prefs.getString("user_name", "");
                    String passwords = prefs.getString("password", "");
                    if (user_name.getText().toString().equals(username) && password.getText().toString().equals(passwords)) {
                        Toast.makeText(getApplicationContext(), "Login Successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Username and password does not exist. Please create the account.", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                finish();

            }
        });
        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                LISessionManager.getInstance(getApplicationContext()).clearSession();
Log.d("linked","linkedin");
                loginHandle();
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                login_type = "google";
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
                signIn();

            }
        });
//                startActivity(new Intent(RegistrationActivity.this,AddFriendActivity.class));
        mCallbackManager = CallbackManager.Factory.create();

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_type = "Facebook";
                FirebaseAuth.getInstance().signOut();

//                LoginManager.getInstance().logInWithPublishPermissions(
//                        LoginActivity.this,
//                        Arrays.asList("publish_actions"));
//                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile", "user_friends"));
                //AccessToken.getCurrentAccessToken().getPermissions();
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
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
//        UpdateTwitterButton();
        client = new TwitterAuthClient();

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.authorize(LoginActivity.this, new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> twitterSessionResult) {
                        Toast.makeText(LoginActivity.this, "Signed in to twitter successful", Toast.LENGTH_LONG).show();
                        signInToFirebaseWithTwitterSession(twitterSessionResult.data);
//                mTwitterBtn.setVisibility(View.VISIBLE);
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Toast.makeText(LoginActivity.this, "Login failed. No internet or No Twitter app found on your phone", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


       /* mTwitterBtn.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(LoginActivity.this, "Signed in to twitter successful", Toast.LENGTH_LONG).show();
                signInToFirebaseWithTwitterSession(result.data);
//                mTwitterBtn.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(LoginActivity.this, "Login failed. No internet or No Twitter app found on your phone", Toast.LENGTH_LONG).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                UpdateTwitterButton();
            }
        });*/

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
        } else if (requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {
            Log.w(TAG, "twitter Login" + requestCode);

            mTwitterBtn.onActivityResult(requestCode, resultCode, data);

        } else if (requestCode == 3672) {
            Log.w(TAG, "twitter Login" + requestCode);

            LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);

        }
        else {
            Log.w(TAG, "facebook Login");
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        Log.d(TAG, "Token No" + acct.getIdToken());
        access_token = acct.getIdToken();
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
                            Toast.makeText(getApplicationContext(), "This email is already used.Please login different mail id", Toast.LENGTH_SHORT).show();
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
            String image = photoUrl.toString();
//            Profile.getCurrentProfile();
            Log.e("social network  detail ", name + " " + email + " " + image + " " + currentUser.getPhoneNumber()); //google social network  detail: Saranya Arumugam saranya.rayaz@gmail.com https://lh4.googleusercontent.com/-POt1IkDNMbI/AAAAAAAAAAI/AAAAAAAAAAA/AMZuucnyGEKIUq_J7I92ZnfPcrovY7JT-Q/s96-c/photo.jpg null
            //facebook social network  detail: Saranya Sixface asaranyamdu@gmail.com https://graph.facebook.com/2868768993405637/picture null

            boolean emailVerified = currentUser.isEmailVerified();
            String uid = currentUser.getUid();
            Log.d("UserID", "USERID " + uid);
            if (email == null) {
//            Log.d(TAG,Profile.getCurrentProfile().get)
            }
            if (login_type == "google") {
                Toast.makeText(LoginActivity.this, "Signed in to google successful from " + name, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(LoginActivity.this, "Signed in to facebook successful from " + name, Toast.LENGTH_LONG).show();

            }
//            social_login(name,email,image,uid,access_token);


        }


    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        access_token = token.getToken();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.d(TAG, "handleFacebookAccessToken:" + token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Log.d(TAG, "" + task.getResult());
                            FirebaseUser user = task.getResult().getUser();
                            Log.d(TAG, "User " + user.getEmail());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void UpdateTwitterButton() {
        Log.d("session", String.valueOf(TwitterCore.getInstance().getSessionManager().getActiveSession()));
        if (TwitterCore.getInstance().getSessionManager().getActiveSession() == null) {
            mTwitterBtn.setVisibility(View.VISIBLE);
        } else {
            mTwitterBtn.setVisibility(View.GONE);
        }
    }

    private void signInToFirebaseWithTwitterSession(TwitterSession session) {
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
                                Toast.makeText(LoginActivity.this, "Signed in firebase twitter successful from " + datas.name, Toast.LENGTH_LONG).show();

                            }

                            @Override
                            public void failure(TwitterException exception) {

                            }
                        });

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Auth firebase twitter failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void loginHandle() {
        LISessionManager.getInstance(getApplicationContext()).init(LoginActivity.this, buildScope(), new AuthListener() {
                    @Override
                    public void onAuthSuccess() {
                        fetchuserData();

                        // Authentication was successful.  You can now do other calls with the SDK.

//                Intent intent=new Intent(MainActivity.this,ProfileActivity.class);
//                startActivity(intent);
                    }
            @Override
            public void onAuthError(LIAuthError error) {
                // Handle authentication errors
                Toast.makeText(getApplicationContext(),"Login Error "+error.toString(),Toast.LENGTH_LONG).show();
                Log.d("error linkedin",error.toString());
            }
        }, true);
    }
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS);
    }

    private void fetchuserData() {
        String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address)";

        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // Success!
                try {
                    JSONObject jsonObject = apiResponse.getResponseDataAsJson();
                    String firstName = jsonObject.getString("firstName");
                    String lastName = jsonObject.getString("lastName");
                    String userEmail = jsonObject.getString("emailAddress");
                    Toast.makeText(getApplicationContext(),"API Error"+firstName,Toast.LENGTH_LONG).show();

//                    StringBuilder stringBuilder = new StringBuilder();
//                    stringBuilder.append("First Name " + firstName + "\n\n");
//                    stringBuilder.append("Last Name " + lastName + "\n\n");
//                    stringBuilder.append("Email " + userEmail);
//
//                    user_detail.setText(stringBuilder);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!
                Toast.makeText(getApplicationContext(),"API Error"+liApiError.toString(),Toast.LENGTH_LONG).show();
            }
        });
    }
}

