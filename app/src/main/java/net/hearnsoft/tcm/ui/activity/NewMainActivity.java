package net.hearnsoft.tcm.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import net.hearnsoft.tcm.R;
import net.hearnsoft.tcm.databinding.ActivityMainNewBinding;
import net.hearnsoft.tcm.ui.interfaces.OnNowPlayingClickListener;
import net.hearnsoft.tcm.utils.Logs;

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
        /*navController = navHostFragment.getNavController();
        navGraph = navController.getNavInflater().inflate(R.navigation.fragment_main);
        navController.setGraph(navGraph);*/
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Only set the graph if this is the first creation, not a configuration change
            if (savedInstanceState == null) {
                navGraph = navController.getNavInflater().inflate(R.navigation.fragment_main);
                navController.setGraph(navGraph);
            }
        } else {
            Logs.e(TAG, "NavHostFragment not found");
        }
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

    public void navigateUpSafely() {
        try {
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.fullPlayerFragment) {
                // We're on the FullPlayerFragment, navigate up safely
                navController.navigateUp();
            }
        } catch (Exception e) {
            Logs.e(TAG, "Navigation up error: " + e.getMessage());
            // Fallback to default back behavior
            onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save navigation state
        if (navController != null) {
            outState.putBundle("nav_controller_state", navController.saveState());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore navigation state if it exists
        if (navController != null && savedInstanceState.containsKey("nav_controller_state")) {
            navController.restoreState(savedInstanceState.getBundle("nav_controller_state"));
        }
    }
}
