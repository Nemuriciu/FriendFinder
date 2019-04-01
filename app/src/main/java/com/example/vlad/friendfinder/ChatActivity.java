package com.example.vlad.friendfinder;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {

    private String id;
    private String username;
    private String[] friends_list;
    private String[] friends_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/source_sans_pro.ttf");

        id = getIntent().getStringExtra("Id");
        username = getIntent().getStringExtra("Username");
        friends_list = getIntent().getStringArrayExtra("Friends");
        friends_id = getIntent().getStringArrayExtra("Ids");

        LinearLayout layout = findViewById(R.id.layout2);

        /* Populate friend list */
        for (int i = 0; i < friends_list.length; i++) {
            FrameLayout l = new FrameLayout(this);
            Button btn = new Button(this);
            //TextView last_refresh = new TextView(this);
            Drawable icon = ContextCompat.getDrawable(this, R.drawable.user);
            Drawable border = ContextCompat.getDrawable(this, R.drawable.button_border);

            btn.setText(friends_list[i]);
            btn.setTextSize(18);
            btn.setTypeface(custom_font);
            btn.setCompoundDrawablePadding(125);
            btn.setBackgroundDrawable(border);
            btn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            btn.setGravity(Gravity.CENTER | Gravity.START);
            btn.setOnClickListener(button_listener);

            l.addView(btn);
            layout.addView(l);
        }
    }

    private View.OnClickListener button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;

            //final Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            //final String friend_id = getUserID(b.getText().toString());
            //intent.putExtra("User", b.getText());

            /*
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
            */
        }
    };
}
