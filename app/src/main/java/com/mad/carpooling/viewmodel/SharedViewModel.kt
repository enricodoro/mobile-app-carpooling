package com.mad.carpooling.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mad.carpooling.model.Trip
import com.mad.carpooling.model.User
import com.mad.carpooling.repository.TripRepository
import com.mad.carpooling.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SharedViewModel(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val trips: MutableLiveData<HashMap<String, Trip>> by lazy {
        MutableLiveData<HashMap<String, Trip>>().also {
            loadTrips()
        }
    }

    private val myTrips: MutableLiveData<HashMap<String, Trip>> by lazy {
        MutableLiveData<HashMap<String, Trip>>().also {
            loadMyTrips()
        }
    }

    private val boughtTrips: MutableLiveData<HashMap<String, Trip>> by lazy {
        MutableLiveData<HashMap<String, Trip>>().also {
            loadBoughtTrips()
        }
    }

    private val interestTrips: MutableLiveData<HashMap<String, Trip>> by lazy {
        MutableLiveData<HashMap<String, Trip>>().also {
            loadInterestedTrips()
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private val othersTrips: MutableLiveData<HashMap<String, Trip>> by lazy {
        MutableLiveData<HashMap<String, Trip>>().also {
            loadOthersTrips()
        }
    }

    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    private fun loadOthersTrips() {

        viewModelScope.launch {
            tripRepository.loadOthersTrips(currentUser).collect {
                othersTrips.postValue(it)
            }
        }

    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    fun getOthersTrips(): LiveData<HashMap<String, Trip>> {
        return othersTrips
    }

    private fun loadInterestedTrips() {
        val db = Firebase.firestore

        db.collection("trips").whereArrayContains("interestedPeople", currentUser.value?.uid!!)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    interestTrips.postValue(HashMap())
                    Log.e("loadInterestedTrips() exception => ", e.toString())
                    return@addSnapshotListener
                }
                val tripsMap: HashMap<String, Trip> = HashMap()
                for (doc in value!!) {
                    tripsMap[doc.id] = doc.toObject(Trip::class.java)
                }
                interestTrips.postValue(tripsMap.filterValues { t -> t.timestamp > Timestamp.now() } as HashMap<String, Trip>?)
            }
    }

    private fun loadBoughtTrips() {
        val db = Firebase.firestore

        db.collection("trips").whereArrayContains("acceptedPeople", currentUser.value?.uid!!)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    boughtTrips.postValue(HashMap())
                    Log.e("loadBoughtTrips() exception => ", e.toString())
                    return@addSnapshotListener
                }
                val tripsMap: HashMap<String, Trip> = HashMap()
                for (doc in value!!) {
                    tripsMap[doc.id] = doc.toObject(Trip::class.java)
                }
                boughtTrips.postValue(tripsMap)
            }
    }

    private fun loadMyTrips() {
        val db = Firebase.firestore
        val currentUserRef =
            FirebaseFirestore.getInstance().document("users/${currentUser.value?.uid}")
        db.collection("trips").whereEqualTo("owner", currentUserRef)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    myTrips.postValue(HashMap())
                    Log.e("loadMyTrips() exception => ", e.toString())
                    return@addSnapshotListener
                }
                val tripsMap: HashMap<String, Trip> = HashMap()
                for (doc in value!!) {
                    tripsMap[doc.id] = doc.toObject(Trip::class.java)
                }
                myTrips.postValue(tripsMap)
            }
    }


/*    private fun loadOthersTrips() {
        val db = Firebase.firestore
        val currentUserRef =
            FirebaseFirestore.getInstance().document("users/${currentUser.value?.uid}" )

        db.collection("trips").whereNotEqualTo("owner", currentUserRef)
            .whereEqualTo("visibility", true)
            //.whereEqualTo("finished", false)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    othersTrips.postValue(HashMap())
                    Log.e("loadTrips() exception => ", e.toString())
                    return@addSnapshotListener
                }
                val tripsMap: HashMap<String, Trip> = HashMap()
                for (doc in value!!) {
                    tripsMap[doc.id] = doc.toObject(Trip::class.java)
                }
                othersTrips.postValue(tripsMap.filterValues { t -> !t.finished && t.timestamp>Timestamp.now() } as HashMap<String, Trip>?)
            }
    } */


    private val currentUser: MutableLiveData<User> by lazy {
        MutableLiveData<User>().also {
            loadUser()
        }
    }

    private fun loadTrips() {
        // Do an asynchronous operation to fetch trips.
        val db = Firebase.firestore
        db.collection("trips").addSnapshotListener { value, e ->
            if (e != null) {
                trips.postValue(HashMap())
                Log.e("loadTrips() exception => ", e.toString())
                return@addSnapshotListener
            }
            val tripsMap: HashMap<String, Trip> = HashMap()
            for (doc in value!!) {
                tripsMap[doc.id] = doc.toObject(Trip::class.java)
            }
            trips.postValue(tripsMap)
        }
    }

    fun getTrips(): LiveData<HashMap<String, Trip>> {
        return trips
    }

    fun getMyTrips(): LiveData<HashMap<String, Trip>> {
        return myTrips
    }

    fun getBoughtTrips(): LiveData<HashMap<String, Trip>> {
        return boughtTrips
    }

    fun getInterestedTrips(): LiveData<HashMap<String, Trip>> {
        return interestTrips
    }


    private fun loadUser() {
        // Do an asynchronous operation to fetch user.
        val db = Firebase.firestore
        db.collection("users").addSnapshotListener { value, e ->
            if (e != null) {
                currentUser.value = null
                Log.e("loadUser() exception => ", e.toString())
                return@addSnapshotListener
            }
            var user = User()
            for (doc in value!!) {
                if (doc.id == Firebase.auth.currentUser?.uid)
                    user = doc.toObject(User::class.java)
            }
            currentUser.postValue(user)
        }

    }

    fun getCurrentUser(): LiveData<User> {
        return currentUser
    }

    fun getUserDoc(userId: String): LiveData<User?> {
        val result = MutableLiveData<User?>()
        viewModelScope.launch {
            val user = userRepository.getUserDoc(userId)
            if (user.isSuccess) {
                result.postValue(user.getOrNull())
            } else {
                result.postValue(null)
            }
        }
        return result
    }

    fun getUserRef(currentUserUid: String) : LiveData<DocumentReference> {
        val res = MutableLiveData<DocumentReference>()
        viewModelScope.launch{
            val result = userRepository.getUserRef(currentUserUid)
            res.postValue(result.getOrNull())
        }
        return res
    }
    
    fun getRatings(user: String, field: String): LiveData<Map<String, ArrayList<Any>>?>{
        val result = MutableLiveData<Map<String, ArrayList<Any>>?>()
        viewModelScope.launch {
            val map = userRepository.getRatings(user, field)
            if (map.isSuccess) {
                result.postValue(map.getOrNull())
            } else {
                result.postValue(null)
            }
        }
        return result
    }

    fun addInterest(trip: String, fieldTrip: String, fieldUser: String, user: String): LiveData<Boolean>
    {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch{
            if(tripRepository.arrayUnionTrip(trip, fieldTrip, user))
                launch{
                    val res = userRepository.arrayUnionUser(user, fieldUser, trip)
                    result.postValue(res)
                }
        }
        return result
    }

    fun removeInterest(trip: String, fieldTrip: String, fieldUser: String, user: String): LiveData<Boolean>
    {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch{
            if(tripRepository.arrayRemoveTrip(trip, fieldTrip, user))
                launch{
                    val res = userRepository.arrayRemoveUser(user, fieldUser, trip)
                    result.postValue(res)
                }
        }
        return result
    }

    fun removeAccepted(trip: String, fieldTrip: String, incrementField: String, user: String, value: Long): LiveData<Boolean>
    {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch{
            val res = tripRepository.getFieldTrip(trip, fieldTrip)
            if (res.isSuccess) {
                val tmpArray = res.getOrNull()
                if (tmpArray != null) {
                    if (tmpArray.contains(user)) {
                        if (tripRepository.arrayRemoveTrip(trip, fieldTrip, user))
                            launch {
                                val res = tripRepository.incrementTrip(trip, incrementField, value)
                                result.postValue(res)
                            }
                    }
                }
            } else {
                result.postValue(false)
            }

        }
        return result
    }

    fun addAccepted(trip: String, fieldTrip: String, incrementField: String, user: String, value: Long): LiveData<Boolean>
    {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch{
            val res = tripRepository.getFieldTrip(trip, fieldTrip)
            if (res.isSuccess) {
                val tmpArray = res.getOrNull()
                if (tmpArray != null) {
                    if (tmpArray.contains(user)) {
                        if (tripRepository.arrayAddTrip(trip, "acceptedPeople", user))
                            launch {
                                val res = tripRepository.incrementTrip(trip, incrementField, value)
                                result.postValue(res)
                            }
                    }
                }
            } else {
                result.postValue(false)
            }

        }
        return result
    }

    fun terminateTrip(trip: String): LiveData<Boolean>
    {
        val res = MutableLiveData<Boolean>()
        viewModelScope.launch{
            val result = tripRepository.terminateTrip(trip)
            res.postValue(result)
        }
        return res
    }

    fun updateRatings(uid: String, role: String, currentUser: String, newArray: ArrayList<Any>): LiveData<Boolean>{
        val res = MutableLiveData<Boolean>()
        viewModelScope.launch{
            val result = userRepository.updateRatings(uid, role, currentUser, newArray)
            res.postValue(result)
        }
        return res
    }

}

