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

public class CallActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private capstone.safeline.adapters.CallAdapter callAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Bottom Nav
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calls);
        bottomNavigationView.setOnItemSelectedListener(navListener);

        // Seeded call log data
        List<String> callLog = new ArrayList<>();
        callLog.add("John Doe - Incoming - 12:30 PM");
        callLog.add("Jane Smith - Outgoing - 11:45 AM");
        callLog.add("Mike Lee - Missed - 10:20 AM");

        recyclerView = findViewById(R.id.recyclerCalls);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        callAdapter = new capstone.safeline.adapters.CallAdapter(callLog);
        recyclerView.setAdapter(callAdapter);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(CallActivity.this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_calls) {
                return true;
            } else if (id == R.id.nav_messages) {
                startActivity(new Intent(CallActivity.this, ChatActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(CallActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        }
    };
}
