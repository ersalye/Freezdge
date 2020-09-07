package com.inved.freezdge.favourites.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.inved.freezdge.R
import com.inved.freezdge.favourites.database.FavouritesRecipes
import com.inved.freezdge.uiGeneral.fragment.BaseFragment
import com.inved.freezdge.utils.App
import com.inved.freezdge.utils.SearchFavouriteButtonListener


class MyRecipesFragment : BaseFragment(),SearchFavouriteButtonListener {

    override fun getLayoutRes(): Int {
        return R.layout.fragment_my_recipes
    }

    companion object{
        var isFavouriteSearchButtonShowed:Boolean=true
    }

    private lateinit var floatingActionButton: FloatingActionButton
    private var searchItem: MenuItem?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setSearchFavouriteButtonListener(this)
        floatingActionButton = view.findViewById(R.id.floating_button)
        floatingActionButton.setOnClickListener {
            launchFilterDialog()
        }
    }

    // manage searchview to find recipe on name
    override fun onPrepareOptionsMenu(menu: Menu) {
        searchItem = menu.findItem(R.id.search_menu)
        searchItem?.isVisible = isFavouriteSearchButtonShowed
        val clearIngredientItem = menu.findItem(R.id.menu_ingredientss_clear)
        clearIngredientItem.isVisible = false
        val searchView = searchItem?.actionView as SearchView
        val edittext =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        edittext.hint = getString(R.string.search_recipe_searchview_label)
        val tf = ResourcesCompat.getFont(App.applicationContext(), R.font.bebasneue_regular)
        edittext.typeface = tf

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                favouriteRecipesItemAdapter.filter(newText)
                favouriteRecipesItemAdapter.itemFilter.filterPredicate =
                    { item: FavouritesRecipes, constraint: CharSequence? ->
                        item.recipeTitle?.contains(
                            constraint.toString(),
                            ignoreCase = true
                        )
                        item.cuisineType?.contains(
                            constraint.toString(),
                            ignoreCase = true
                        ) == true
                    }
                return true
            }
        })
        return super.onPrepareOptionsMenu(menu)
    }

    // launch dialog to choose dish type and filter recipes
    private fun launchFilterDialog() {
        val builder = MaterialAlertDialogBuilder(activity)
        builder.setTitle(getString(R.string.array_dialog_title))
            .setItems(
                getFilterTextItems().toArray(arrayOfNulls<String>(0))
            ) { _, which ->
                // The 'which' argument contains the index position of selected item
                when (which) {
                    0 -> filterDishType(activity?.getString(R.string.array_filter_entry_search))
                    1 -> filterDishType(activity?.getString(R.string.array_filter_plat_search))
                    2 -> filterDishType(activity?.getString(R.string.array_filter_dessert_search))
                    3 -> filterDishType(activity?.getString(R.string.array_filter_cocktail_search))
                    4 -> {
                        filterDishType("")
                    }
                }
            }
        builder.create()
        builder.show()

    }

    //get programmatically text for filter with number of them
    private fun getFilterTextItems():ArrayList<String?> {
        val filterTextItems: ArrayList<String?> = ArrayList()
        filterTextItems.add(activity?.getString(R.string.array_filter_entry_title,getNumberItemForFilterText(activity?.getString(R.string.array_filter_entry_search))))
        filterTextItems.add(activity?.getString(R.string.array_filter_plat_title,getNumberItemForFilterText(activity?.getString(R.string.array_filter_plat_search))))
        filterTextItems.add(activity?.getString(R.string.array_filter_dessert_title,getNumberItemForFilterText(activity?.getString(R.string.array_filter_dessert_search))))
        filterTextItems.add(activity?.getString(R.string.array_filter_cocktail_title,getNumberItemForFilterText(activity?.getString(R.string.array_filter_cocktail_search))))
        filterTextItems.add(activity?.getString(R.string.array_filter_all_title, setFavouriteList.size))
        return filterTextItems
    }

    //get the number or favourites recipe who are for example entry, or cocktail etc
    private fun getNumberItemForFilterText(text:String?):Int?{
        val numberCount:ArrayList<String?> = ArrayList()
        for(recipes in setFavouriteList){
            if(recipes.dishType?.contains(text.toString(), true)==true){
                numberCount.add(text)
            }
        }
        return numberCount.size
    }


    // the given text only filter on dishType for recipes from retrofit or database
    private fun filterDishType(filterText: String?) {
        setFavouriteListFilter.clear()

        for(recipes in setFavouriteList){
            if(recipes.dishType?.contains(filterText.toString(), true)==true){
                setFavouriteListFilter.add(recipes)
            }
        }

        if(filterText.equals("")){
            fillFavouriteAdapterFilter(setFavouriteList)
        }else{
            fillFavouriteAdapterFilter(setFavouriteListFilter)
        }


    }

    // clear adapter and fill it with the filter recipes
    private fun fillFavouriteAdapterFilter(setfavouritelist: MutableList<FavouritesRecipes>) {
        favouriteRecipesItemAdapter.clear()

        for (recipes in setfavouritelist) {
            favouriteRecipesItemAdapter.add(recipes)
        }
        favouritesRecipesNumberFilter()
    }

    // determines the recipes filter number to show
    private fun favouritesRecipesNumberFilter(){

        recipesFavouritesNumberSize= setFavouriteListFilter.size
        if(recipesFavouritesNumberSize!=1){
            numberRecipesTextview.text = getString(
                R.string.recipe_list_number,
                recipesFavouritesNumberSize
            )
        }else{
            numberRecipesTextview.text = getString(
                R.string.recipe_list_number_one,
                recipesFavouritesNumberSize
            )
        }
    }

    override fun showSearchButton() {
        isFavouriteSearchButtonShowed =true

        searchItem?.isVisible = true
    }

    override fun hideSearchButton() {
        isFavouriteSearchButtonShowed =false
        searchItem?.isVisible = false
    }

}