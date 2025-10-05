package capstone.safeline;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import android.content.Intent;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private capstone.safeline.adapters.MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Bottom nav
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_messages);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        // Seeded messages
        List<String> messages = new ArrayList<>();
        messages.add("John: Hey, how are you?");
        messages.add("Jane: Meeting at 2 PM");
        messages.add("Mike: Call me back!");

        recyclerView = findViewById(R.id.recyclerMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new capstone.safeline.adapters.MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(ChatActivity.this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_calls) {
                startActivity(new Intent(ChatActivity.this, CallActivity.class));
                return true;
            } else if (id == R.id.nav_messages) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(ChatActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        }
    };
}
