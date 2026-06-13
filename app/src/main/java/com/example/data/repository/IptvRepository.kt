package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.ChannelDao
import com.example.data.database.SponsorDao
import com.example.data.model.Channel
import com.example.data.model.Sponsor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class IptvRepository(
    private val channelDao: ChannelDao,
    private val sponsorDao: SponsorDao
) {
    val allActiveChannels: Flow<List<Channel>> = channelDao.getAllChannelsFlow()
    val allActiveSponsors: Flow<List<Sponsor>> = sponsorDao.getAllActiveSponsors()

    suspend fun getAllRawChannels(): List<Channel> = withContext(Dispatchers.IO) {
        channelDao.getAllRawChannels()
    }

    fun getChannelsByCategory(category: String): Flow<List<Channel>> {
        return channelDao.getChannelsByCategoryFlow(category)
    }

    suspend fun insertChannels(channels: List<Channel>) = withContext(Dispatchers.IO) {
        channelDao.insertChannels(channels)
    }

    suspend fun updateChannelStatus(url: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        channelDao.updateChannelStatus(url, isActive)
    }

    suspend fun updateChannelValidation(url: String, isActive: Boolean, responseTimeMs: Long) = withContext(Dispatchers.IO) {
        channelDao.updateChannelValidation(url, isActive, responseTimeMs)
    }

    suspend fun insertSponsor(sponsor: Sponsor) = withContext(Dispatchers.IO) {
        sponsorDao.insertSponsor(sponsor)
    }

    suspend fun deleteSponsor(sponsor: Sponsor) = withContext(Dispatchers.IO) {
        sponsorDao.deleteSponsor(sponsor)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        channelDao.deleteAllChannels()
        sponsorDao.deleteAllSponsors()
    }

    /**
     * Seeds default IPTV channels in Bangladesh, India and International categories
     * when the application state is empty on startup.
     */
    suspend fun seedDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val initialSponsors = listOf(
            Sponsor(name = "SPONSOR 1", description = "SPONSOR: Premium Bangladesh Live TV Stream. Experience 4K clarity with zero buffering.", isActive = true),
            Sponsor(name = "SPONSOR 2", description = "বিজ্ঞাপন: আপনার ব্র্যান্ডকে লক্ষ লক্ষ দর্শকের মোবাইলে ছড়িয়ে দিন আজই! যোগাযোগ: s-iptv@portal.net", isActive = true)
        )
        initialSponsors.forEach { sponsorDao.insertSponsor(it) }

        val defaultChannels = listOf(
            // Bangladesh (দেশী খবর ও বিনোদন)
            Channel(
                url = "https://live-free.jamuna.tv/hls/jamunahlshd.m3u8",
                name = "Jamuna TV HD (যমুনা টিভি)",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEj79D40h1Vst0nFscIWeP-oZ5R4o6DRE_pQ2fQnNox6V_c92OPhY41WvO3K3mBof3P_vIs6e3D4-v_b6_Z_vX8S/s1600/jamuna_tv.png",
                category = "News (খবর)",
                country = "Bangladesh",
                resolution = "1080p",
                responseTimeMs = 120L // Initial guess
            ),
            Channel(
                url = "https://somoytv-ott.somoynews.com/hls/somoytv.m3u8",
                name = "Somoy News (সময় টিভি)",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgoq5N9V-Z269s5Wn8_bM62LqIuV_m5vD6o8r8o4z/s1600/somoy_news.png",
                category = "News (খবর)",
                country = "Bangladesh",
                resolution = "1080p",
                responseTimeMs = 150L
            ),
            Channel(
                url = "http://103.119.100.22:1935/ch24/ch24.stream/playlist.m3u8",
                name = "Channel 24 (চ্যানেল ২৪)",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjoQ2_V9M9_y9N5_W_9O9_2_S_N_O_z/s1600/channel24.png",
                category = "Entertainment (বিনোদন)",
                country = "Bangladesh",
                resolution = "720p",
                responseTimeMs = 180L
            ),
            Channel(
                url = "https://btvlive.btv.gov.bd/hls/live.m3u8",
                name = "BTV World (বিটিভি ওয়ার্ল্ড)",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiPq_69D_v9N6h1B9S6r75D6_2O_Z_q/s1600/btv_world.png",
                category = "News (খবর)",
                country = "Bangladesh",
                resolution = "720p",
                responseTimeMs = 210L
            ),
            Channel(
                url = "http://103.119.100.22:1935/sangsad/sangsad.stream/playlist.m3u8",
                name = "Sangsad TV (সংসদ টিভি)",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiS_h6r88_V_q95Z_Z_G_V_6_Y/s1600/sangsad_tv.png",
                category = "News (খবর)",
                country = "Bangladesh",
                resolution = "720p",
                responseTimeMs = 240L
            ),
            Channel(
                url = "https://itv-live.singularitybd.com/itv/live/playlist.m3u8",
                name = "Independent TV (ইনডিপেনডেন্ট)",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgv4_969_F_m65O2s_Y_l_W2O_q/s1600/independent_tv.png",
                category = "News (খবর)",
                country = "Bangladesh",
                resolution = "1080p",
                responseTimeMs = 200L
            ),

            // India (ভারতীয় মিডিয়া)
            Channel(
                url = "https://zeenews.akamaized.net/hls/live/2012117/zeenews/index.m3u8",
                name = "Zee News India",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjPq5_V_G_269S9L_53vGz-V-782q_4v_z/s1600/zee_news.png",
                category = "News (খবর)",
                country = "India",
                resolution = "720p",
                responseTimeMs = 400L
            ),
            Channel(
                url = "https://aajtak.akamaized.net/hls/live/2014138/asb/aajtak/master.m3u8",
                name = "Aaj Tak Live",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEg_N86X_U_969t2n97D_C_s_Z/s1600/aaj_tak.png",
                category = "News (খবর)",
                country = "India",
                resolution = "1080p",
                responseTimeMs = 450L
            ),
            Channel(
                url = "https://indiatoday.akamaized.net/hls/live/2014521/indiatoday/master.m3u8",
                name = "India Today Live",
                logo = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiPq5_G_E_z_X5P8G_h1_W_H_z/s1600/india_today.png",
                category = "News (খবর)",
                country = "India",
                resolution = "1080p",
                responseTimeMs = 480L
            ),

            // FIFA & Sports (ফিফা এবং খেলাধুলা)
            Channel(
                url = "https://vcdn.solasport.tv/hls/eurosport.m3u8",
                name = "Eurosport HD (Sola Live)",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Eurosport_Logo.svg/1200px-Eurosport_Logo.svg.png",
                category = "FIFA (ফিফা)",
                country = "Global",
                resolution = "1080p",
                responseTimeMs = 310L
            ),
            Channel(
                url = "https://solasport.tv/hls/bein.m3u8",
                name = "beIN Sports Global Link",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/BeIN_Sports_logo.svg/1200px-BeIN_Sports_logo.svg.png",
                category = "FIFA (ফিফা)",
                country = "Global",
                resolution = "1080p",
                responseTimeMs = 320L
            ),
            Channel(
                url = "https://vcdn.solasport.tv/hls/sports.m3u8",
                name = "Sola Sports Premium",
                logo = "https://solasport.tv/assets/logo.png",
                category = "FIFA (ফিফা)",
                country = "Global",
                resolution = "1085p",
                responseTimeMs = 350L
            ),

            // Global/World channels (বৈশ্বিক চ্যানেলসমূহ)
            Channel(
                url = "https://live-hls-web-aje.getaj.net/AJE/01.m3u8",
                name = "Al Jazeera English News",
                logo = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Al_Jazeera_English_logo.svg/1200px-Al_Jazeera_English_logo.svg.png",
                category = "News (খবর)",
                country = "Global",
                resolution = "1080p",
                responseTimeMs = 500L
            ),
            Channel(
                url = "https://static.france24.com/live/F24_EN_LO_HLS/live_tv.m3u8",
                name = "France 24 Live EN",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/France_24_logo.svg/1200px-France_24_logo.svg.png",
                category = "News (খবর)",
                country = "Global",
                resolution = "720p",
                responseTimeMs = 550L
            ),
            Channel(
                url = "https://dwstream72-lh.akamaihd.net/i/dwstream72_live@123556/master.m3u8",
                name = "DW News English Live",
                logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d1/Deutsche_Welle_logo_2012.svg/1200px-Deutsche_Welle_logo_2012.svg.png",
                category = "News (খবর)",
                country = "Global",
                resolution = "720p",
                responseTimeMs = 600L
            )
        )

        channelDao.insertChannels(defaultChannels)
        Log.d("IptvRepository", "Database seeded successfully with ${defaultChannels.size} preset channels.")
    }

    /**
     * Checks if a stream URL is active and measures its connection latency (responseTimeMs).
     */
    suspend fun verifyChannelLinkWithLatency(url: String): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(1500, TimeUnit.MILLISECONDS)
                .readTimeout(1500, TimeUnit.MILLISECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                val durationMs = System.currentTimeMillis() - startTime
                val isSuccess = response.isSuccessful || response.code in 200..399
                Pair(isSuccess, if (isSuccess) durationMs else 99999L)
            }
        } catch (e: Exception) {
            Log.e("IptvRepository", "Failed validation for $url: ${e.message}")
            Pair(false, 99999L)
        }
    }
}
