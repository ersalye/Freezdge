package com.inved.freezdge.recipes.view

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.inved.freezdge.R
import com.inved.freezdge.favourites.database.FavouritesRecipes
import com.inved.freezdge.favourites.database.FavouritesRecipes_
import com.inved.freezdge.recipes.database.Recipes
import com.inved.freezdge.utils.App
import com.inved.freezdge.utils.Domain
import com.inved.freezdge.utils.GlideApp
import com.mikepenz.fastadapter.FastAdapter
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor

class ViewHolderRecipesDatabase (view: View) : FastAdapter.ViewHolder<Recipes>(view){

    var label: TextView = view.findViewById(R.id.fragment_recipes_list_item_label)
    var preparationTime: TextView =
        view.findViewById(R.id.fragment_recipes_list_item_preparation_time_text)
    var kcal: TextView = view.findViewById(R.id.fragment_recipes_list_item_kcal)
    var imageItem: ImageView = view.findViewById(R.id.fragment_recipes_list_item_image)
    var imageFavourite: ImageView =
        view.findViewById(R.id.fragment_recipe_list_favorite_selected_or_not)
    var proportionText: TextView =
        view.findViewById(R.id.fragment_recipes_list_item_matching)
    override fun bindView(item: Recipes, payloads: MutableList<Any>) {

        label.text = item.recipeTitle
        preparationTime.text=item.totalrecipeTime

        if(item.recipeCalories.isNullOrEmpty()){
            kcal.text = App.resource().getString(R.string.recipe_list_item_kcal_notknow)
        }else{
            kcal.text = item.recipeCalories
        }

        val proportionInPercent:Int= Domain.ingredientsFavouriteMatchingMethod(item.recipeIngredients)
        proportionText.text="$proportionInPercent %"

        Log.d("debago","PROPORTION IN PERCENT FOR ${item.recipeTitle} is $proportionInPercent")
        if(proportionInPercent in 90..99){
            proportionText.setBackgroundResource(R.drawable.border_green)
        }else if (proportionInPercent in 50..94){
            proportionText.setBackgroundResource(R.drawable.border_orange)
        }else if (proportionInPercent in 0..49){
            proportionText.setBackgroundResource(R.drawable.border_red)
        }

        GlideApp.with(App.applicationContext())
            .load(item.recipePhotoUrl)
            .centerCrop()
            .into(imageItem)

        if(isRecipeIdIsPresent(item.recipeTitle)!!){
            imageFavourite.setImageResource(R.drawable.ic_favorite_selected_24dp)
        }else{
            imageFavourite.setImageResource(R.drawable.ic_favorite_not_selected_24dp)
        }

    }

    override fun unbindView(item: Recipes) {
        label.text = null
        preparationTime.text = null
        proportionText.text = null
        kcal.text = null
        imageItem.setImageDrawable(null)
    }

    fun getFavouritesRecipesBox(): Box<FavouritesRecipes> {
        val boxStore: BoxStore = App.ObjectBox.boxStore
        return boxStore.boxFor()

    }

    fun isRecipeIdIsPresent(recipeId:String?):Boolean? {
        val favouritesRecipes: FavouritesRecipes? =
            getFavouritesRecipesBox()
                .query().equal(FavouritesRecipes_.recipeId, recipeId!!)
                .build().findUnique()
        return favouritesRecipes!=null
    }
}