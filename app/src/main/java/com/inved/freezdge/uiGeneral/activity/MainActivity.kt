package com.inved.freezdge.uiGeneral.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.inved.freezdge.R

class MainActivity : BaseActivity() {

    private val navController by lazy { findNavController(R.id.navHost) }
    private val bottomNavigationView by lazy { findViewById<BottomNavigationView>(R.id.activity_main_bottom_navigation) }
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id:Int = intent.getIntExtra("BACKPRESS",0)
        initToolbar(navController)
        setUpNavigationBottom(navController,id)

    }

    override fun getLayoutContentViewID(): Int {
        return R.layout.activity_main
    }

    //INITIALIZATION


    fun initToolbar(navController: NavController){

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.my_ingredients_list_fragment,
                R.id.my_recipes_fragment,
                R.id.all_recipes_fragment,
                R.id.social_media_fragment
            )
        )

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)

    }

    private fun setUpNavigationBottom(navController: NavController,id:Int) {
        NavigationUI.setupWithNavController(bottomNavigationView,navController)
        if(id==1){
            bottomNavigationView.menu.findItem(R.id.action_to_all_recipes_fragment).isChecked = true
            navController.navigate(R.id.action_to_all_recipes_fragment)
        }/*else if(id==0){
            bottomNavigationView.menu.findItem(R.id.action_to_my_recipes_fragment).isChecked = true
        }*/

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        val item: MenuItem = menu.findItem(R.id.menu_logout)
        item.setOnMenuItemClickListener {
            signOut()
            true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        startActivity(LoginActivity.getLaunchIntent(this))
        FirebaseAuth.getInstance().signOut();
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

}
