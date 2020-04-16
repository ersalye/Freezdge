package com.inved.freezdge.recipes.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.inved.freezdge.R
import com.inved.freezdge.favourites.database.FavouritesRecipes
import com.inved.freezdge.favourites.database.FavouritesRecipes_
import com.inved.freezdge.model.recipes.Hit
import com.inved.freezdge.utils.App
import com.inved.freezdge.utils.Domain
import com.inved.freezdge.utils.GlideApp
import com.mikepenz.fastadapter.FastAdapter
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import kotlin.math.roundToInt


class ViewHolder(view: View) : FastAdapter.ViewHolder<Hit>(view) {

    var label: TextView = view.findViewById(R.id.fragment_recipes_list_item_label)
    var preparationTime: TextView =
        view.findViewById(R.id.fragment_recipes_list_item_preparation_time_text)
    var kcal: TextView = view.findViewById(R.id.fragment_recipes_list_item_kcal)
    var imageItem: ImageView = view.findViewById(R.id.fragment_recipes_list_item_image)
    var imageFavourite: ImageView =
        view.findViewById(R.id.fragment_recipe_list_favorite_selected_or_not)

    override fun bindView(item: Hit, payloads: MutableList<Any>) {

        label.text = item.recipe?.label
        preparationTime.text=Domain.preparationTime(item.recipe?.totalTime)

        kcal.text = item.recipe?.calories!!.div(10).roundToInt().toString()

        GlideApp.with(App.applicationContext())
            .load(item.recipe?.image)
            .into(imageItem)

        if(isRecipeIdIsPresent(item.recipe?.uri)!!){
            imageFavourite.setImageResource(R.drawable.ic_favorite_selected_24dp)
        }else{
            imageFavourite.setImageResource(R.drawable.ic_favorite_not_selected_24dp)
        }

    }

    override fun unbindView(item: Hit) {
        label.text = null
        preparationTime.text = null
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