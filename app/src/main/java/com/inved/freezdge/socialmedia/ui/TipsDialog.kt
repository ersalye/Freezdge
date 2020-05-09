package com.inved.freezdge.socialmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.inved.freezdge.R
import com.inved.freezdge.socialmedia.firebase.Post
import com.inved.freezdge.socialmedia.firebase.PostHelper
import com.inved.freezdge.utils.App
import com.inved.freezdge.utils.Domain
import kotlinx.android.synthetic.main.dialog_fullscreen_add_tips.*
import java.util.*

class TipsDialog : DialogFragment() {

    companion object {
        //final values
        const val TAG = "TIPS"
        private const val KEY_TIP = "tip_update"
        private const val KEY_TIP_ID = "tip_id"

        @JvmStatic
        fun newInstance(param1: Int, param2: String) =
            TipsDialog().apply {
                arguments = Bundle().apply {
                    putInt(KEY_TIP, param1)
                    putString(KEY_TIP_ID, param2)
                }
            }
    }

    private var validateButton: TextView? = null
    private var cancelButton: ImageButton? = null
    private var dialogTitle: TextView? = null
    private lateinit var postIdUpdate: String
    // --------------
    // LIFE CYCLE AND VIEW MODEL
    // --------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View =
            inflater.inflate(R.layout.dialog_fullscreen_add_tips, container, false)
        validateButton = view.findViewById(R.id.validate_button)
        dialogTitle = view.findViewById(R.id.dialogTitle)
        cancelButton = view.findViewById(R.id.close_button)
        val id: Int? = arguments?.getInt(KEY_TIP, 0)
        postIdUpdate= arguments?.getString(KEY_TIP_ID, null)!!
        if (postIdUpdate.isNotEmpty()) {
            fillTip(postIdUpdate)
        }
        initializeMethods(id)

        return view
    }

    private fun fillTip(postId: String?) {
        postId?.let {
            PostHelper.getPost(it)?.get()?.addOnCompleteListener { task ->
                if (task.result != null) {
                    if (task.result!!.documents.isNotEmpty()) {

                        val post: Post =
                            task.result!!.documents[0].toObject(Post::class.java)!!
                        dialogTitle?.text= App.resource().getString(R.string.tips_dialog_update)
                        titleEdittext?.setText(post.titleAstuce)
                        descriptionEdittext?.setText(post.descriptionAstuce)
                    }
                }
            }?.addOnFailureListener { }
        }
    }

    private fun initializeMethods(id: Int?) {

        cancelButton?.setOnClickListener {
            cancelButton?.startAnimation(Domain.animation())
            dialog?.dismiss()
        }
        validateButton?.setOnClickListener {
            validateButton?.startAnimation(Domain.animation())
            updatePost(id) }
    }

    // --------------
// POST
// --------------
    private fun updatePost(id: Int?) {

        if (titleEdittext?.text.toString().isEmpty()) {
            titleEdittext?.error = getString(R.string.set_error_title_tips)
        } else if (descriptionEdittext.text.toString().isEmpty()) {
            descriptionEdittext.error = getString(R.string.set_error_description_tips)
        } else {
            val title = titleEdittext?.text.toString()
            val description = descriptionEdittext?.text.toString()

            var uid: String? = null
            if (FirebaseAuth.getInstance().currentUser != null) {
                uid = FirebaseAuth.getInstance().currentUser?.uid
            }

            //id=0 on crée le post, sinon on update
            if (id == 0) {
                if (uid != null) {
                    val postId: String = Domain.createRandomString()
                    //create post in firebase
                    PostHelper.createPost(
                        postId,
                        Calendar.getInstance().time as Date,
                        title,
                        description,
                        null,
                        uid,
                        getString(R.string.social_media_post_type_tips),
                        0
                    )

                    Toast.makeText(
                        activity,
                        getString(R.string.toast_created_tip),
                        Toast.LENGTH_LONG
                    ).show()

                }
            } else {
                if (uid != null) {
                    //update agent in firebase
                    PostHelper.updateTitleAstuce(title, postIdUpdate)
                    PostHelper.updateDescriptionAstuce(description, postIdUpdate)

                    Toast.makeText(
                        activity,
                        getString(R.string.toast_updated_tip),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


            //to close the dialog
            if (dialog != null) {
                dialog?.dismiss()
            }
        }
    }


}