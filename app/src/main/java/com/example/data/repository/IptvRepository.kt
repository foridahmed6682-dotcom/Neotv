package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.ChannelDao
import com.example.data.database.SponsorDao
import com.example.data.database.AppDatabase
import com.example.data.model.Channel
import com.example.data.model.Sponsor
import com.example.data.parser.M3uParser
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class IptvRepository(
    private val context: Context,
    private val channelDao: ChannelDao,
    private val sponsorDao: SponsorDao
) {
    val allChannelsFlow: Flow<List<Channel>> = channelDao.getAllChannelsFlow()
    val activeSponsorsFlow: Flow<List<Sponsor>> = sponsorDao.getActiveSponsorsFlow()
    val allSponsorsFlow: Flow<List<Sponsor>> = sponsorDao.getAllSponsorsFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("IptvRepository", "Firebase initialization failed: ${e.message}")
            null
        }
    }

    suspend fun insertSponsorLocal(sponsor: Sponsor) = withContext(Dispatchers.IO) {
        sponsorDao.insertSponsor(sponsor)
    }

    suspend fun deleteSponsorLocal(id: String) = withContext(Dispatchers.IO) {
        sponsorDao.deleteSponsorById(id)
    }

    /**
     * Set up default offline cache values for sponsors if database is empty.
     */
    suspend fun seedDefaultSponsorsIfEmpty() = withContext(Dispatchers.IO) {
        val currentSponsors = mutableListOf<Sponsor>()
        // Let's check from custom Flow, or we can query DB. Here, we can seed default premium sponsors.
        val defaultSponsors = listOf(
            Sponsor(
                id = "sponsor_default_1",
                imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=400&q=80",
                text = "Premium Bangladesh Sports IPTV Portal. Experience live football, cricket HD streaming with zero latency!",
                linkUrl = "https://iptv-org.github.io/",
                isActive = true,
                updatedAt = System.currentTimeMillis()
            ),
            Sponsor(
                id = "sponsor_default_2",
                imageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=400&q=80",
                text = "Watch Independent, Somoy, BTV, and local Bangladesh News channels anywhere, optimized for remote and mobile viewing.",
                linkUrl = "https://github.com/iptv-org/iptv",
                isActive = true,
                updatedAt = System.currentTimeMillis() - 1000
            )
        )
        sponsorDao.insertSponsors(defaultSponsors)
    }

    /**
     * Pull sponsors from Firebase Firestore in a thread-safe helper, saving them to Room local cache.
     */
    suspend fun fetchSponsorsFromFirestore(): Result<List<Sponsor>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore is not initialized or missing google-services.json"))

        suspendCancellableCoroutine { continuation ->
            db.collection("sponsors")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val sponsors = mutableListOf<Sponsor>()
                    for (doc in querySnapshot) {
                        try {
                            val id = doc.id
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            val text = doc.getString("text") ?: ""
                            val linkUrl = doc.getString("linkUrl") ?: ""
                            val isActive = doc.getBoolean("isActive") ?: true
                            val updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()

                            sponsors.add(
                                Sponsor(
                                    id = id,
                                    imageUrl = imageUrl,
                                    text = text,
                                    linkUrl = linkUrl,
                                    isActive = isActive,
                                    updatedAt = updatedAt
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("IptvRepository", "Error parsing sponsor doc: ${doc.id}", e)
                        }
                    }
                    continuation.resume(Result.success(sponsors))
                }
                .addOnFailureListener { exception ->
                    Log.e("IptvRepository", "Firestore fetch sponsors failed", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    /**
     * Synergized sync: tries to fetch sponsors from Cloud, saving them locally, otherwise falls back to local cache.
     */
    suspend fun syncSponsors() {
        fetchSponsorsFromFirestore().onSuccess { cloudSponsors ->
            if (cloudSponsors.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    sponsorDao.deleteAllSponsors()
                    sponsorDao.insertSponsors(cloudSponsors)
                }
            }
        }.onFailure {
            Log.w("IptvRepository", "Could not fetch sponsors from cloud, using local cached sponsors.")
            // Ensure we at least have our premium default sponsors seeded if local db is empty
            seedDefaultSponsorsIfEmpty()
        }
    }

    /**
     * Load IPTV playlist from custom URL or presets, parsing items and caching into local DB.
     */
    suspend fun loadPlaylist(url: String, forcedCountry: String? = null): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d("IptvRepository", "Loading playlist: $url")
            val parsedChannels = M3uParser.parseFromUrl(url, forcedCountry)
            if (parsedChannels.isNotEmpty()) {
                // Keep existing channels if the user wants, or replace.
                // In modern hybrid design, we replace the channels so we get the fresh playlist!
                channelDao.deleteAllChannels()
                channelDao.insertChannels(parsedChannels)
                Log.d("IptvRepository", "Inserted ${parsedChannels.size} channels into Room cache.")
                Result.success(parsedChannels.size)
            } else {
                Result.failure(Exception("M3U Playlist parsed 0 channels. Please verify URL source layout."))
            }
        } catch (e: Exception) {
            Log.e("IptvRepository", "Error loading playlist", e)
            Result.failure(e)
        }
    }

    /**
     * Admin Firestore additions, updates & deletions. Employs best of both worlds by writing to Firestore,
     * and always updating local cache as well so the changes reflect immediately.
     */
    suspend fun addSponsor(sponsor: Sponsor): Result<Unit> = withContext(Dispatchers.IO) {
        // Local Room persistence is absolute first priority
        sponsorDao.insertSponsor(sponsor)

        val db = firestore ?: return@withContext Result.success(Unit) // Firestore failure is handled gracefully

        suspendCancellableCoroutine { continuation ->
            val data = hashMapOf(
                "imageUrl" to sponsor.imageUrl,
                "text" to sponsor.text,
                "linkUrl" to sponsor.linkUrl,
                "isActive" to sponsor.isActive,
                "updatedAt" to sponsor.updatedAt
            )
            db.collection("sponsors").document(sponsor.id)
                .set(data)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.e("IptvRepository", "Firestore write sponsor failed", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun deleteSponsor(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        sponsorDao.deleteSponsorById(id)

        val db = firestore ?: return@withContext Result.success(Unit)

        suspendCancellableCoroutine { continuation ->
            db.collection("sponsors").document(id)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.e("IptvRepository", "Firestore deletion failed", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    // --- Admin Management ---

    suspend fun fetchAdminsFromFirestore(): Result<List<String>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore not initialized"))

        suspendCancellableCoroutine { continuation ->
            db.collection("admins")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val admins = querySnapshot.documents.mapNotNull { it.id }
                    continuation.resume(Result.success(admins))
                }
                .addOnFailureListener {
                    continuation.resume(Result.failure(it))
                }
        }
    }

    suspend fun addAdminToFirestore(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore not initialized"))

        suspendCancellableCoroutine { continuation ->
            db.collection("admins").document(email)
                .set(mapOf("email" to email))
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }
    }

    suspend fun removeAdminFromFirestore(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore not initialized"))

        suspendCancellableCoroutine { continuation ->
            db.collection("admins").document(email)
                .delete()
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }
    }

    suspend fun updateChannelStatus(url: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        channelDao.updateChannelStatus(url, isActive)
    }

    suspend fun updateChannelValidation(url: String, isActive: Boolean, responseTimeMs: Long) = withContext(Dispatchers.IO) {
        channelDao.updateChannelValidation(url, isActive, responseTimeMs)
    }

    suspend fun verifyChannelLink(url: String): Boolean = withContext(Dispatchers.IO) {
        verifyChannelLinkWithLatency(url).first
    }

    suspend fun verifyChannelLinkWithLatency(url: String): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(1500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(1500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
            val request = okhttp3.Request.Builder()
                .url(url)
                .head()
                .build()
            client.newCall(request).execute().use { response ->
                val durationMs = System.currentTimeMillis() - startTime
                val isSuccess = response.isSuccessful || response.code in 200..399
                Pair(isSuccess, if (isSuccess) durationMs else 99999L)
            }
        } catch (e: java.lang.Exception) {
            Pair(false, 99999L)
        }
    }
}
