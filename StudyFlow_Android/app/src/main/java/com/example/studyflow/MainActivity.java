package com.example.studyflow;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.studyflow.databinding.ActivityMainBinding;
import com.example.studyflow.ui.dashboard.DashboardFragment;
import com.example.studyflow.ui.grades.GradeListFragment;
import com.example.studyflow.ui.subjects.SubjectListFragment; // Import necesario
import com.example.studyflow.ui.tasks.TaskListFragment;
import com.example.studyflow.ui.user.UserConfigFragment; // Import necesario

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar);

        // Configurar Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        // Cargar Dashboard por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
            binding.navView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) fragment = new DashboardFragment();
        else if (id == R.id.nav_tasks) fragment = new TaskListFragment();
        else if (id == R.id.nav_subjects) fragment = new SubjectListFragment();
        else if (id == R.id.nav_grades) fragment = new GradeListFragment();
        else if (id == R.id.nav_profile) fragment = new UserConfigFragment();

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Necesario para que el botón "Atrás" cierre el menú si está abierto
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}