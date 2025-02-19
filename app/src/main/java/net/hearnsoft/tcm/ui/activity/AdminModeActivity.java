package net.hearnsoft.tcm.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.hearnsoft.tcm.databinding.ActivityAdminModeBinding;

public class AdminModeActivity extends AppCompatActivity {
    private ActivityAdminModeBinding binding;

    private String userToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userToken = getIntent().getStringExtra("user_token");
        binding = ActivityAdminModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toast.makeText(this, userToken, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
