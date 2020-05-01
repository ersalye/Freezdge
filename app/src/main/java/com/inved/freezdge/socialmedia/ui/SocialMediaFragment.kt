package com.inved.freezdge.socialmedia.ui

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.inved.freezdge.R
import com.inved.freezdge.socialmedia.firebase.Post
import com.inved.freezdge.socialmedia.firebase.PostHelper
import com.inved.freezdge.socialmedia.firebase.User
import com.inved.freezdge.socialmedia.firebase.UserHelper
import com.inved.freezdge.socialmedia.view.PostsAdapter
import com.inved.freezdge.utils.App
import com.inved.freezdge.utils.Domain
import com.inved.freezdge.utils.LoaderListener
import kotlinx.android.synthetic.main.fragment_social_media.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class SocialMediaFragment : Fragment(), PostsAdapter.ClickListener, LoaderListener {

    private lateinit var mRecyclerPostsAdapter: PostsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var addPhotoGallery: ImageView
    private lateinit var addPhotoCamera: ImageView
    private lateinit var addTipImage: ImageView
    private lateinit var photoProfile: ImageView
    private lateinit var addPhotoGalleryText: TextView
    private lateinit var addPhotoCameraText: TextView
    private lateinit var addTipText: TextView
    private lateinit var topDescription: TextView
    private var loader: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // in here you can do logic when backPress is clicked
                nestedScrollView.smoothScrollTo(0, 0)

            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView: View = inflater.inflate(R.layout.fragment_social_media, container, false)
        recyclerView = mView.findViewById(R.id.recyclerview)
        addPhotoGallery = mView.findViewById(R.id.image_photo_post)
        addPhotoCamera = mView.findViewById(R.id.image_camera_post)
        addTipImage = mView.findViewById(R.id.image_tips_post)
        nestedScrollView = mView.findViewById(R.id.nested_scroll_view)
        addPhotoGalleryText = mView.findViewById(R.id.title_photo_post)
        addPhotoCameraText = mView.findViewById(R.id.title_camera_post)
        addTipText = mView.findViewById(R.id.title_tips_post)
        photoProfile = mView.findViewById(R.id.profile_image)
        topDescription = mView.findViewById(R.id.profile_activity_top_description)
        loader = mView.findViewById(R.id.animation_view_container)
        //RecyclerView initialization
        // topDescription.startAnimation(Domain.animationFromTransparency())
        // photoProfile.startAnimation(Domain.animationFromTransparency())
        showLoader()
        initButtons()
        initProfil()
        displayAllPosts()
        return mView
    }

    override fun onResume() {
        super.onResume()
        initProfil()
        displayAllPosts()
    }

    private fun initProfil() {

        UserHelper.getUser(FirebaseAuth.getInstance().currentUser?.uid)?.get()
            ?.addOnCompleteListener { task ->
                if (task.result != null) {
                    if (task.result!!.documents.isNotEmpty()) {

                        val user: User =
                            task.result!!.documents[0].toObject(User::class.java)!!

                        topDescription.text =
                            getString(R.string.social_media_description, user.firstname)

                        //to upload a photo on Firebase storage
                        if (user.photoUrl != null) {
                            photoProfile.let {
                                activity?.let { it1 ->
                                    Glide.with(it1)
                                        .load(user.photoUrl)
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.ic_anon_user_48dp)
                                        .into(it)
                                }
                            }

                            hideLoader()

                        } else {
                            photoProfile.let {
                                activity?.let { it1 ->
                                    Glide.with(it1)
                                        .load(R.drawable.ic_anon_user_48dp)
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.ic_anon_user_48dp)
                                        .into(it)
                                }
                            }
                        }

                    }
                }
            }?.addOnFailureListener {
            Log.e(
                "debago",
                "Problem during the user creation"
            )
        }
    }

    private fun initButtons() {
        addPhotoCamera.setOnClickListener { onClickAddPhotoWithPermissionCheck(1, "") }
        addPhotoCameraText.setOnClickListener { onClickAddPhotoWithPermissionCheck(1, "") }
        addPhotoGallery.setOnClickListener { onClickAddPhotoGalleryWithPermissionCheck() }
        addPhotoGalleryText.setOnClickListener { onClickAddPhotoGalleryWithPermissionCheck() }
        addTipImage.setOnClickListener { onClickAddTips(0, "") }
        addTipText.setOnClickListener { onClickAddTips(0, "") }
        photoProfile.setOnClickListener { onClickUpdateProfil() }
    }

    private fun displayAllPosts() {
        Log.d("debago", "in display all posts")
        mRecyclerPostsAdapter = PostsAdapter(
            generateOptionsForAdapter(PostHelper.getAllPosts()), this
        )

        //Choose how to display the list in the RecyclerView (vertical or horizontal)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )


        recyclerView.adapter = mRecyclerPostsAdapter


    }

    override fun onStart() {
        super.onStart()
        mRecyclerPostsAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (mRecyclerPostsAdapter != null) {
            mRecyclerPostsAdapter.stopListening()
        }
    }


    // Create options for RecyclerView from a Query
    private fun generateOptionsForAdapter(query: Query): FirestoreRecyclerOptions<Post?> {
        Log.d("debago", "in generate options for adapter")
        return FirestoreRecyclerOptions.Builder<Post>()
            .setQuery(query, Post::class.java)
            .setLifecycleOwner(this)
            .build()
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun onClickAddPhoto(value: Int, postId: String) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        val previous = activity?.supportFragmentManager?.findFragmentByTag(PhotoDialog.TAG)
        if (previous != null) {
            transaction?.remove(previous)
        }
        transaction?.addToBackStack(null)

        val dialogFragment = PhotoDialog.newInstance(value, postId)
        if (transaction != null) {
            dialogFragment.show(transaction, PhotoDialog.TAG)
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onClickAddPhotoGallery() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        val previous = activity?.supportFragmentManager?.findFragmentByTag(PhotoDialog.TAG)
        if (previous != null) {
            transaction?.remove(previous)
        }
        transaction?.addToBackStack(null)

        val dialogFragment = PhotoDialog.newInstance(2, "")
        if (transaction != null) {
            dialogFragment.show(transaction, PhotoDialog.TAG)
        }
    }


    private fun onClickAddTips(value: Int, postId: String) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        val previous = activity?.supportFragmentManager?.findFragmentByTag(TipsDialog.TAG)
        if (previous != null) {
            transaction?.remove(previous)
        }
        transaction?.addToBackStack(null)

        val dialogFragment = TipsDialog.newInstance(value, postId)
        if (transaction != null) {
            dialogFragment.show(transaction, TipsDialog.TAG)
        }
    }


    fun onClickUpdateProfil() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        val previous = activity?.supportFragmentManager?.findFragmentByTag(ProfilDialog.TAG)
        if (previous != null) {
            transaction?.remove(previous)
        }
        transaction?.addToBackStack(null)

        val dialogFragment = ProfilDialog.newInstance("profil")
        if (transaction != null) {
            dialogFragment.show(transaction, ProfilDialog.TAG)
        }
    }


    override fun onRequestPermissionsResult(
        rc: Int,
        permissions: Array<out String>,
        results: IntArray
    ) {
        // Java: "MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, rc, results);"
        // I'm not satisfied with the method signature here, since it's too similar to the Android one.
        // However, the signature is already pretty long, so I'm open for ideas.
        this.onRequestPermissionsResult(rc, results)
    }

    private fun launchAlertDialog(postId: String) {
        val builder = MaterialAlertDialogBuilder(activity)
        builder.setTitle(
            App.resource().getString(R.string.social_media_post_delete_title_dialog)
        )
        builder.setMessage(
            App.resource().getString(R.string.social_media_post_delete_message_dialog)
        )

        builder.setPositiveButton(App.resource().getString(R.string.Yes)) { _, _ ->
            PostHelper.deletePost(postId)
            Toast.makeText(
                App.applicationContext(),
                App.resource().getString(R.string.social_media_post_delete_confirmation_dialog),
                Toast.LENGTH_SHORT
            ).show()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    override fun onClickListener(value: Int, postId: String) {
        when (value) {
            1 -> {
                launchAlertDialog(postId)
            }
            2 -> {
                onClickAddTips(value, postId)
            }
            3 -> {
                onClickAddPhotoWithPermissionCheck(value, postId)
            }
        }


    }


    // --------------------
    // CALLBACK
    // --------------------
    override fun onDataChanged() {
        // 7 - Show TextView in case RecyclerView is empty
        Log.d("debago", "in on data changed ${mRecyclerPostsAdapter.itemCount}")
        if (mRecyclerPostsAdapter.itemCount == 0) {
            no_post_found.visibility = View.VISIBLE
        } else {
            no_post_found.visibility = View.GONE
        }
    }

    override fun showLoader() {
        Log.d("debago", "in show loader")
        loader?.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        Log.d("debago", "in hide loader")
        loader?.visibility = View.GONE
        nestedScrollView?.visibility = View.VISIBLE
    }


}