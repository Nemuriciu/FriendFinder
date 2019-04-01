package com.example.vlad.friendfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static long back_pressed;
    private static long count;
    private String username;
    private String id;
    private String[] friends_list;
    private String[] friends_id;
    private TextView[] timestamp_text;
    private LinearLayout layout;
    private DatabaseReference db;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location myLocation;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseDatabase.getInstance().getReference();
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/source_sans_pro.ttf");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        swipeRefresh = findViewById(R.id.swiperefresh);

        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshContent();
                }
            });

        id = getIntent().getStringExtra("Id");
        username = getIntent().getStringExtra("Username");
        friends_list = getIntent().getStringArrayExtra("Friends");
        friends_id = getIntent().getStringArrayExtra("Ids");
        layout = findViewById(R.id.layout);

        count = friends_list.length;
        timestamp_text = new TextView[(int)count];

        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        Arrays.sort(friends_list);

        /* Populate friend list */
        for (int i = 0; i < friends_list.length; i++) {
            FrameLayout l = new FrameLayout(this);
            Button btn = new Button(this);
            TextView last_refresh = new TextView(this);
            Drawable icon = ContextCompat.getDrawable(this, R.drawable.user);
            Drawable border = ContextCompat.getDrawable(this, R.drawable.button_border);

            timestamp_text[i] = last_refresh;

            btn.setText(friends_list[i]);
            btn.setTextSize(18);
            btn.setTypeface(custom_font);
            btn.setCompoundDrawablePadding(125);
            btn.setBackgroundDrawable(border);
            btn.setCompoundDrawablesWithIntrinsicBounds(icon,null, null,null);
            btn.setGravity(Gravity.CENTER | Gravity.START);
            btn.setOnClickListener(button_listener);

            last_refresh.setTextSize(15);
            last_refresh.setTypeface(Typeface.SANS_SERIF);
            last_refresh.setGravity(Gravity.CENTER | Gravity.END);
            last_refresh.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            ViewCompat.setTranslationZ(last_refresh, 50);
            l.addView(btn);
            l.addView(last_refresh);
            layout.addView(l);
        }

        findViewById(R.id.chat).setOnClickListener(button_listener);
    }

    private View.OnClickListener button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;

            if (b.getText() == "Chat") {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("Id", id);
                intent.putExtra("User", username);
                intent.putExtra("Friends", friends_list);
                intent.putExtra("Ids", friends_id);
                startActivity(intent);
            }

            final Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            final String friend_id = getUserID(b.getText().toString());
            intent.putExtra("User", b.getText());

            db.child("users").child(friend_id).child("location").child("latitude").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    intent.putExtra("Latitude", dataSnapshot.getValue().toString());

                    db.child("users").child(friend_id).child("location").child("longitude").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            intent.putExtra("Longitude", dataSnapshot.getValue().toString());
                            startActivity(intent);
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }
    };

    private void addUserToDb(String userId, String name) {
        db.child("users").child(userId).child("name").setValue(name);
        db.child("users").child(userId).child("location").child("latitude").setValue(myLocation.getLatitude());
        db.child("users").child(userId).child("location").child("longitude").setValue(myLocation.getLongitude());
        db.child("users").child(userId).child("timestamp").setValue(Calendar.getInstance().getTimeInMillis());
    }

    private void updateUserLocation(String userId) {
        db.child("users").child(userId).child("location").child("latitude").setValue(myLocation.getLatitude());
        db.child("users").child(userId).child("location").child("longitude").setValue(myLocation.getLongitude());
        db.child("users").child(userId).child("timestamp").setValue(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        db.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(id))
                    addUserToDb(id, username);
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        updateTimestamps();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        updateUserLocation(id);
    }

    public long getTime(long start, long end) {
        return TimeUnit.MILLISECONDS.toMinutes(Math.abs(end - start));
    }

    public String getUserID(String username) {
        for (int i = 0; i < friends_list.length; i++) {
            if (friends_list[i].equals(username))
                return friends_id[i];
        }

        return null;
    }

    private void updateTimestamps() {
        for (int i = 0; i < friends_list.length; i++) {
            final TextView last_refresh = timestamp_text[i];

            db.child("users").child(friends_id[i]).child("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long ts = (long)dataSnapshot.getValue();
                    long minutes = getTime(ts, Calendar.getInstance().getTimeInMillis());

                    if (minutes == 0) minutes = 1;
                    String time = minutes + "m ago ";

                    last_refresh.setText(time);
                    last_refresh.setTextColor((ts < 10)? Color.parseColor("#00dd00"):
                                                         Color.parseColor("#dd0000"));
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }
    }

    @Override
    public void onBackPressed(){
        if (back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
        }
        else{
            Toast.makeText(getBaseContext(), "Press once again to exit", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    private void refreshContent() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTimestamps();
            }
        }, 100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                swipeRefresh.setRefreshing(false);
            }
        }, 500);
    }
}
