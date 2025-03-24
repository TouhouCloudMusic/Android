package net.hearnsoft.tcm.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ActivityMainNewBinding;
import net.hearnsoft.tcm.ui.interfaces.OnNowPlayingClickListener;

public class NewMainActivity extends AppCompatActivity implements OnNowPlayingClickListener {
    private static final String TAG = NewMainActivity.class.getSimpleName();
    private ActivityMainNewBinding binding;

    public OnNowPlayingClickListener getNowPlayingClickListener() {
        return this;
    }

    private NavHostFragment navHostFragment;
    private NavController navController;
    private NavGraph navGraph;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        navController = navHostFragment.getNavController();
        navGraph = navController.getNavInflater().inflate(R.navigation.fragment_main);
        navController.setGraph(navGraph);
    }

    @Override
    public void onNowPlayingClick() {
        NavController navController = navHostFragment.getNavController();
        navController.navigate(R.id.fullPlayerFragment, null, new NavOptions.Builder()
                .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim)
                .build());
    }
}
