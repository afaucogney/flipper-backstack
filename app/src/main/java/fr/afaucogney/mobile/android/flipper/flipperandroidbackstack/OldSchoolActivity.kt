package fr.afaucogney.mobile.android.flipper.flipperandroidbackstack

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import fr.afaucogney.mobile.android.flipper.flipperandroidbackstack.ui.dashboard.DashboardFragment
import fr.afaucogney.mobile.android.flipper.flipperandroidbackstack.ui.home.HomeFragment
import fr.afaucogney.mobile.android.flipper.flipperandroidbackstack.ui.notifications.NotificationsFragment

class OldSchoolActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_old)
//        registerForContextMenu(findViewById(R.id.nav_view))
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, HomeFragment())
            .addToBackStack(null)
            .commit()

        findViewById<BottomNavigationView>(R.id.nav_view).setOnNavigationItemSelectedListener { item->
            when (item.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment())
                        .addToBackStack("1")
                        .commit()
                    true
                }
                R.id.navigation_dashboard -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, DashboardFragment())
                        .addToBackStack("2")
                        .commit()
                    true
                }
                else -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, NotificationsFragment())
                        .addToBackStack("3")
                        .commit()
                    true
                }
            }
        }
    }
}