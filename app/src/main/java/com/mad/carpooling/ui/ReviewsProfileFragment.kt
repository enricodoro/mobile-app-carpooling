package com.mad.carpooling.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mad.carpooling.R
import com.mad.carpooling.repository.TripRepository
import com.mad.carpooling.repository.UserRepository
import com.mad.carpooling.viewmodel.SharedViewModel
import com.mad.carpooling.viewmodel.SharedViewModelFactory
import com.taufiqrahman.reviewratings.BarLabels
import com.taufiqrahman.reviewratings.RatingReviews
import kotlin.math.roundToInt


class ReviewsProfileFragment : Fragment(R.layout.fragment_reviews_profile) {
    private lateinit var rv: RecyclerView
    private lateinit var tv_stars: TextView
    private lateinit var tv_reviews: TextView
    private lateinit var rb_ratings: RatingBar
    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(
            TripRepository(),
            UserRepository()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val role = arguments?.getString("role")
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Reviews as ${role}"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initReviewsList(view)
    }

    private fun initReviewsList(view: View) {
        val emptyView = view.findViewById<TextView>(R.id.no_reviews_available)

        val uid = arguments?.getString("uid")
        val role = arguments?.getString("role")
        rv = view.findViewById<RecyclerView>(R.id.reviews_list)
        tv_stars = view.findViewById<TextView>(R.id.tv_number_stars)
        tv_reviews = view.findViewById<TextView>(R.id.tv_number_reviews)
        rb_ratings = view.findViewById<RatingBar>(R.id.rb_fragment_review)

        rv.layoutManager = LinearLayoutManager(context)
        rv.isNestedScrollingEnabled = false //prevent toolbar to expand on scroll

        lateinit var userList: Array<String>
        var stars = intArrayOf(0, 0, 0, 0, 0)
        var totStars = 0

        val ratingReviews = view.findViewById(R.id.rating_reviews) as RatingReviews

        val colors = intArrayOf(
            Color.parseColor("#0e9d58"),
            Color.parseColor("#bfd047"),
            Color.parseColor("#ffc105"),
            Color.parseColor("#ef7e14"),
            Color.parseColor("#d36259")
        )

        /*
        val colors: Array<Pair<Int,Int>> = arrayOf(
            Pair(Color.parseColor("#0e9d58"), Color.parseColor("#6ec49a")),
            Pair(Color.parseColor("#bfd047"), Color.parseColor("#d8e290")),
            Pair(Color.parseColor("#ffc105"), Color.parseColor("#ffd969")),
            Pair(Color.parseColor("#ef7e14"), Color.parseColor("#f5b172")),
            Pair(Color.parseColor("#d36259"), Color.parseColor("#e4a09b"))
        )*/

        if (role == "driver") {
            sharedViewModel.getRatings(uid!!,"driverRatings").observe(viewLifecycleOwner, Observer { mapRatingDriver ->
                if (mapRatingDriver != null) {
                    userList = mapRatingDriver.keys.toTypedArray()

                    val reviewAdapter = ReviewAdapter(userList, mapRatingDriver, sharedViewModel)
                    rv.adapter = reviewAdapter
                    if (reviewAdapter.itemCount == 0) {//from getItemCount
                        emptyView.isVisible = true
                        tv_reviews.text = "0.0"
                    } else {
                        for (us in userList) {
                            if (mapRatingDriver[us]?.get(0) == 1L) {
                                stars[4]++; totStars += 1
                            }
                            if (mapRatingDriver[us]?.get(0) == 2L) {
                                stars[3]++; totStars += 2
                            }
                            if (mapRatingDriver[us]?.get(0) == 3L) {
                                stars[2]++; totStars += 3
                            }
                            if (mapRatingDriver[us]?.get(0) == 4L) {
                                stars[1]++; totStars += 4
                            }
                            if (mapRatingDriver[us]?.get(0) == 5L) {
                                stars[0]++; totStars += 5
                            }
                        }

                        tv_reviews.text = mapRatingDriver.size.toString()
                        tv_stars.text =
                            ("%.1f".format((totStars.toFloat() / mapRatingDriver.size)))
                        rb_ratings.rating = totStars.toFloat() / mapRatingDriver.size
                    }

                    val raters = if (mapRatingDriver.isNotEmpty()) intArrayOf(
                        ((stars[0].toFloat() / mapRatingDriver.size) * 100).roundToInt(),
                        ((stars[1].toFloat() / mapRatingDriver.size) * 100).roundToInt(),
                        ((stars[2].toFloat() / mapRatingDriver.size) * 100).roundToInt(),
                        ((stars[3].toFloat() / mapRatingDriver.size) * 100).roundToInt(),
                        ((stars[4].toFloat() / mapRatingDriver.size) * 100).roundToInt()
                    ) else intArrayOf(0, 0, 0, 0, 0)

                    ratingReviews.createRatingBars(100, BarLabels.STYPE1, colors, raters)
                }
            })
        } else {
            sharedViewModel.getRatings(uid!!, "passengerRatings")
                .observe(viewLifecycleOwner, Observer { mapRatingPassenger ->
                    if (mapRatingPassenger != null) {
                        userList = mapRatingPassenger.keys.toTypedArray()

                        val reviewAdapter =
                            ReviewAdapter(userList, mapRatingPassenger, sharedViewModel)
                        rv.adapter = reviewAdapter

                        if (reviewAdapter.itemCount == 0) {//from getItemCount
                            emptyView.isVisible = true
                            tv_reviews.text = "0.0"
                        } else {
                            for (us in userList) {
                                when {
                                    mapRatingPassenger[us]?.get(0) == 1L -> {
                                        stars[4]++; totStars += 1
                                    }
                                    mapRatingPassenger[us]?.get(0) == 2L -> {
                                        stars[3]++; totStars += 2
                                    }
                                    mapRatingPassenger[us]?.get(0) == 3L -> {
                                        stars[2]++; totStars += 3
                                    }
                                    mapRatingPassenger[us]?.get(0) == 4L -> {
                                        stars[1]++; totStars += 4
                                    }
                                    mapRatingPassenger[us]?.get(0) == 5L -> {
                                        stars[0]++; totStars += 5
                                    }
                                }
                            }
                            tv_reviews.text = mapRatingPassenger.size.toString()
                            tv_stars.text =
                                ("%.1f".format((totStars.toFloat() / mapRatingPassenger.size)))
                            rb_ratings.rating = totStars.toFloat() / mapRatingPassenger.size
                        }
                        val raters =
                            if (mapRatingPassenger.isNotEmpty()) {
                                intArrayOf(
                                    ((stars[0].toFloat() / mapRatingPassenger.size) * 100).roundToInt(),
                                    ((stars[1].toFloat() / mapRatingPassenger.size) * 100).roundToInt(),
                                    ((stars[2].toFloat() / mapRatingPassenger.size) * 100).roundToInt(),
                                    ((stars[3].toFloat() / mapRatingPassenger.size) * 100).roundToInt(),
                                    ((stars[4].toFloat() / mapRatingPassenger.size) * 100).roundToInt()
                                )
                            } else {
                                intArrayOf(0, 0, 0, 0, 0)
                            }

                        ratingReviews.createRatingBars(100, BarLabels.STYPE1, colors, raters)
                    }
                })
        }
    }

    class ReviewAdapter(
        private val userList: Array<String>,
        private val map: Map<String, ArrayList<Any>>,
        private val model: SharedViewModel
    ) :
        RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

        class ReviewViewHolder(v: View, private val model: SharedViewModel) :
            RecyclerView.ViewHolder(v) {
            val ivProfile = v.findViewById<ImageView>(R.id.iv_ratings_profile_pic)
            val tvNickname = v.findViewById<TextView>(R.id.tv_ratings_nickname)
            val rb = v.findViewById<RatingBar>(R.id.rb_ratings_ratingbar)
            val comment = v.findViewById<TextView>(R.id.tv_ratings_comment)

            fun bind(userId: String, userReview: ArrayList<Any>?, holder: ReviewViewHolder) {
                /*db.collection("users").document(userId).get().addOnSuccessListener {
                    tvNickname.text = it.get("nickname").toString()
                    Glide.with(holder.itemView).load(it?.get("imageUserRef"))
                        .into(ivProfile)
                }*/
                model.getUserDoc(userId)
                    .observe(itemView.context as LifecycleOwner, Observer { user ->
                        tvNickname.text = user?.nickname
                        Glide.with(holder.itemView).load(user?.imageUserRef)
                            .into(ivProfile)
                    })
                rb.rating = userReview?.get(0).toString().toFloat()
                comment.text = userReview?.get(1).toString()
                /* if ellipsize is set
                comment.setOnClickListener {
                    comment.maxLines = 100
                }
                */
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val layout =
                LayoutInflater.from(parent.context).inflate(R.layout.ratings_layout, parent, false)
            return ReviewViewHolder(layout, model)
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            val userId = userList[position]
            val userReview = map[userId]
            holder.bind(userId, userReview, holder)
        }

        override fun getItemCount(): Int {
            return userList.size
        }

    }

}
