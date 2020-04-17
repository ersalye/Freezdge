package com.inved.freezdge.utils

import android.util.Log
import com.inved.freezdge.R
import com.inved.freezdge.ingredientslist.database.Ingredients
import com.inved.freezdge.ingredientslist.database.Ingredients_
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import java.util.*
import kotlin.math.roundToInt

class Domain {

    companion object{

        private var nbIngredientInRecipe:Double=0.0
        private var nbIngredientInFridge:Double=0.0

        fun preparationTime(time:Double?):String{

            when {
                time==0.0 -> {
                    return App.resource().getString(R.string.recipe_list_item_no_time_known)
                }
                time!! <=60.0 -> {
                    return App.resource().getString(R.string.recipe_list_item_time_min, time.roundToInt())
                }
                else -> {
                    val timeInt=time.toInt()
                    val hours: Int = timeInt / 60 //since both are ints, you get an int

                    val minutes: Int = timeInt % 60
                    return App.resource().getString(R.string.recipe_list_item_time_min_hours,hours, minutes)
                }
            }

        }

        fun ingredientsFavouriteMatchingMethod(input: String?): Int {

            initNumberIngredientsFromString(input)

                return correspondanceCalcul(input)

        }


        fun ingredientsMatchingMethod(input: List<String?>?): Int {

            initNumberIngredients(input)

            if (input == null || input.isEmpty()){
                return 0
            }else{
                val sb = StringBuilder()
                for (i in input.indices) {
                    sb.append(input[i])

                    // if not the last item
                    if (i != input.size - 1) {
                        sb.append(",")
                    }
                }

                return correspondanceCalcul(sb.toString())
            }

        }

        fun initNumberIngredients(input: List<String?>?){
            //init ingredient number for recipe
            if(input!=null){
                nbIngredientInRecipe=input.size.toDouble()
            }

            if(input!=null){
                if(input.contains("water") || input.contains("eau")){
                    nbIngredientInRecipe= nbIngredientInRecipe?.minus(1)
                }

                if(input.contains("salt") || input.contains("sel")){
                    nbIngredientInRecipe= nbIngredientInRecipe?.minus(1)
                }
            }

            nbIngredientInFridge=0.0
            Log.d("debago","1. number ingredient in recipe is $nbIngredientInRecipe")

        }



        fun initNumberIngredientsFromString(input: String?){
            //init ingredient number for recipe from favourite string
            val poiList = ArrayList(
                Arrays.asList(
                    *input!!.split(",").toTypedArray()
                )
            )

                nbIngredientInRecipe=poiList.size.toDouble()



                if(poiList.contains("water") || poiList.contains("eau")){
                    nbIngredientInRecipe= nbIngredientInRecipe?.minus(1)
                }

                if(poiList.contains("salt") || poiList.contains("sel")){
                    nbIngredientInRecipe= nbIngredientInRecipe?.minus(1)
                }


            nbIngredientInFridge=0.0
            Log.d("debago","1. number ingredient in recipe is $nbIngredientInRecipe")

        }

        fun proportionCalcul(nbIngredientInFridge: Double,nbIngredientInRecipe: Double): Int {
            val n:Int= ((nbIngredientInFridge/nbIngredientInRecipe)*100).roundToInt()
           return ((n + 4) / 5 * 5);
        }

        fun correspondanceCalcul(input: String?): Int {

            Log.d("debago","2. input is $input")
            for (i in getAllIngredientBySelected()){
               if(i.name?.let { input?.contains(it) }!!){
                   Log.d("debago","3. input contains ${i.name}")
                   nbIngredientInFridge= nbIngredientInFridge?.plus(1)
               }
            }
            Log.d("debago","4. ingredients in fridge is $nbIngredientInFridge")
            if(nbIngredientInFridge!=0.0 && nbIngredientInRecipe!=0.0){
                return proportionCalcul(nbIngredientInFridge, nbIngredientInRecipe)
            }else{
                return 0
            }
        }

        fun getIngredientsBox(): Box<Ingredients> {
            val boxStore: BoxStore = App.ObjectBox.boxStore
            return boxStore.boxFor()
        }

        fun getAllIngredientBySelected(): List<Ingredients> {
            // query all notes, sorted a-z by their text (http://greenrobot.org/objectbox/documentation/queries/)
            return getIngredientsBox()
                .query().equal(Ingredients_.selectedIngredient,true).order(Ingredients_.name).build().find()
        }

    }




}