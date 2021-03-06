package com.inved.freezdge.recipes.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.inved.freezdge.R
import com.inved.freezdge.favourites.database.FavouritesRecipes
import com.inved.freezdge.favourites.database.FavouritesRecipes_
import com.inved.freezdge.recipes.model.Hit
import com.inved.freezdge.utils.App
import com.inved.freezdge.utils.Domain
import com.inved.freezdge.utils.GlideUtils
import com.mikepenz.fastadapter.FastAdapter
import io.objectbox.kotlin.boxFor
import kotlin.math.roundToInt


class ViewHolderRecipesRetrofit(view: View) : FastAdapter.ViewHolder<Hit>(view) {

    var domain=Domain()
    var label: TextView = view.findViewById(R.id.title)
    var preparationTime: TextView =
        view.findViewById(R.id.fragment_recipes_list_item_preparation_time_text)
    private var kcal: TextView = view.findViewById(R.id.description)
    private var cuisineType: TextView = view.findViewById(R.id.fragment_recipes_list_cuisine_type)
    private var dishType: TextView = view.findViewById(R.id.fragment_recipes_list_dish_type)
    private var imageItem: ImageView = view.findViewById(R.id.image)
    var imageFavourite: ImageView =
        view.findViewById(R.id.favorite_image)
    var proportionText: TextView =
        view.findViewById(R.id.fragment_recipes_list_item_matching)

    override fun bindView(item: Hit, payloads: MutableList<Any>) {

        label.text = item.recipe?.label
        preparationTime.text=domain.preparationTime(item.recipe?.totalTime)

        cuisineType.text= item.recipe?.cuisineType?.get(0)?.let { domain.uppercaseFirstCharacter(it) }
        if(item.recipe?.dishType?.get(0).equals("Main course",true)){
            dishType.text=App.resource().getString(R.string.array_filter_plat)
        }else{
            dishType.text= item.recipe?.dishType?.get(0)?.let { domain.uppercaseFirstCharacter(it) }
        }

        kcal.text = item.recipe?.calories?.div(10)?.roundToInt().toString()

        // we calcul the proportion of ingredients matching between our selected ingredients and the ingredients in the recipe.
        val proportionInPercent:Int=domain.ingredientsMatchingMethod(item.recipe?.ingredientLines)
        proportionText.text=App.resource().getString(R.string.recipe_matching_percent,proportionInPercent)

        // We attribute different color according to the matching value
        when (proportionInPercent) {
            in 80..100 -> {
                proportionText.setBackgroundResource(R.drawable.border_green)
            }
            in 50..79 -> {
                proportionText.setBackgroundResource(R.drawable.border_orange)
            }
            in 0..49 -> {
                proportionText.setBackgroundResource(R.drawable.border_red)
            }
        }

        // we show photo from the network call
        GlideUtils.loadPhotoWithGlideCenterCropUrl(item.recipe?.image,imageItem)

        // We detect if the recipe is in our favourite and update UI according to
        if(isRecipeIdIsPresent(item.recipe?.uri)){
            imageFavourite.setImageResource(R.drawable.ic_favorite_selected_24dp)
        }else{
            imageFavourite.setImageResource(R.drawable.ic_favorite_not_selected_24dp)
        }

    }

    override fun unbindView(item: Hit) {
        label.text = null
        preparationTime.text = null
        proportionText.text = null
        kcal.text = null
        imageItem.setImageDrawable(null)
    }

    private fun isRecipeIdIsPresent(recipeId:String?):Boolean {

        return if(recipeId!=null){
            val favouritesRecipes: FavouritesRecipes? =
                App.ObjectBox.boxStore.boxFor<FavouritesRecipes>()
                    .query().equal(FavouritesRecipes_.recipeId, recipeId)
                    .build().findUnique()
            favouritesRecipes!=null
        }else{
            false
        }
    }

}

