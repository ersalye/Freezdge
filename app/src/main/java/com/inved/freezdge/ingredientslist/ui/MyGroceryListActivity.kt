package com.inved.freezdge.ingredientslist.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inved.freezdge.R
import com.inved.freezdge.ingredientslist.viewmodel.IngredientsViewModel
import com.inved.freezdge.injection.Injection
import com.inved.freezdge.uiGeneral.activity.BaseActivity
import com.inved.freezdge.uiGeneral.activity.MainActivity
import com.inved.freezdge.uiGeneral.fragment.BaseFragment
import com.inved.freezdge.utils.App
import io.objectbox.BoxStore.context

class MyGroceryListActivity: BaseActivity() {

    private lateinit var ingredientsViewmodel: IngredientsViewModel
    private lateinit var chipGroup: ChipGroup
    private lateinit var notFoundTeextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseFragment.setlistRetrofit.clear()
        initToolbarBaseActivity(R.string.toolbar_grocery_list)
        chipGroup =findViewById(R.id.chipGroup)
        notFoundTeextView =findViewById(R.id.not_found)
       initViewModel()
        setupChips()
    }

    private fun initViewModel() {
        val viewModelFactory = Injection.providesViewModelFactory(App.ObjectBox.boxStore)
        ingredientsViewmodel = ViewModelProviders.of(
            this,
            viewModelFactory
        ).get(IngredientsViewModel::class.java)
    }

    override fun getLayoutContentViewID(): Int {
        return R.layout.activity_my_grocery_list
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("BACKPRESS", 0)
        startActivity(intent)
    }

    private fun setupChips() {
        ingredientsViewmodel.getIngredientsForGrocery()
            .observe(this, Observer { result ->
                if(result!=null){
                    if(result.size!=0){
                        notFoundTeextView.visibility = View.GONE
                        chipGroup.removeAllViews()
                        for (myresult in result){
                            val chip= Chip(chipGroup.context)
                            val chipDrawable = ChipDrawable.createFromAttributes(
                                chipGroup.context,
                                null,
                                0,
                                R.style.Widget_MaterialComponents_Chip_Entry
                            )
                            chip.setChipDrawable(chipDrawable)
                            chip.text=myresult.name
                            chip.closeIcon=context?.let {
                                ContextCompat.getDrawable(context as Context,R.drawable.ic_clear_grey_24dp) }
                            // Set chip close icon click listener
                            chip.setOnCloseIconClickListener{
                                launchAlertDialog(chip)
                            }
                            chip.isClickable=true
                            chipGroup.addView(chip)
                        }
                    }else{
                        notFoundTeextView.visibility = View.VISIBLE
                        notFoundTeextView.text = getString(R.string.no_item_found_grocery)
                    }
                }
            })
    }

    private fun launchAlertDialog(chip:Chip) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.menu_grocery_list))
        builder.setMessage(getString(R.string.dialog_question_grocery))

        builder.setPositiveButton(getString(R.string.Yes)) { _, _ ->
            Toast.makeText(applicationContext,
                getString(R.string.grocery_list_removed,chip.text), Toast.LENGTH_SHORT).show()

            ingredientsViewmodel.updateIngredientSelectedByName(chip.text.toString(),true)
            ingredientsViewmodel.updateIngredientSelectedForGroceryByName(chip.text.toString(),false)
            TransitionManager.beginDelayedTransition(chipGroup)
            chipGroup.removeView(chip)
        }

        builder.setNegativeButton(android.R.string.no) { dialog, _ ->
            Toast.makeText(applicationContext,
                getString(R.string.dialog_cancel_action), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.show()
    }
}