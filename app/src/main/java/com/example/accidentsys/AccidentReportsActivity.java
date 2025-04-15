package com.example.accidentsys;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.accidentsys.databinding.ActivityAccidentReportsBinding;
import com.example.accidentsys.fragments.AccidentListFragment;
import com.example.accidentsys.fragments.AccidentMapFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AccidentReportsActivity extends AppCompatActivity {

    private ActivityAccidentReportsBinding binding;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccidentReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Accident Reports");
        }

        // Initialize ViewPager and TabLayout
        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;

        // Set up the adapter for the ViewPager
        AccidentPagerAdapter pagerAdapter = new AccidentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect the TabLayout with the ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("All Reports");
                    break;
                case 1:
                    tab.setText("Map View");
                    break;
            }
        }).attach();

        // Set up refresh button
        binding.fabRefresh.setOnClickListener(v -> {
            // Get the current fragment and refresh its data
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
            if (fragment instanceof AccidentListFragment) {
                ((AccidentListFragment) fragment).fetchAccidentReports();
            } else if (fragment instanceof AccidentMapFragment) {
                ((AccidentMapFragment) fragment).fetchAccidentReports();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ViewPager adapter for the accident reports tabs
    private static class AccidentPagerAdapter extends FragmentStateAdapter {

        public AccidentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AccidentListFragment();
                case 1:
                    return new AccidentMapFragment();
                default:
                    return new AccidentListFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Two tabs: List and Map
        }
    }
}
