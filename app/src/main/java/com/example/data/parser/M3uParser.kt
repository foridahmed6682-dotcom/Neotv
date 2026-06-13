package com.example.data.parser

import com.example.data.model.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader

object M3uParser {
    private val client = OkHttpClient()

    suspend fun parseFromUrl(playlistUrl: String, forcedCountry: String? = null): List<Channel> {
        val request = Request.Builder()
            .url(playlistUrl)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch M3U playlist from: $playlistUrl (HTTP ${response.code})")
        }

        val bodyString = response.body?.string() ?: return emptyList()
        return parseString(bodyString, playlistUrl, forcedCountry)
    }

    fun parseString(m3uContent: String, sourceUrl: String = "", forcedCountry: String? = null): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(m3uContent))
        var line: String? = reader.readLine()

        // M3U files usually start with #EXTM3U
        var extinfInfo: String? = null

        var channelCount = 1

        while (line != null) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("#EXTINF:")) {
                extinfInfo = trimmedLine
            } else if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                if (extinfInfo != null) {
                    val channel = parseExtinf(extinfInfo, trimmedLine, sourceUrl, forcedCountry, channelCount)
                    if (channel != null) {
                        channels.add(channel)
                        channelCount++
                    }
                    extinfInfo = null
                }
            }
            line = reader.readLine()
        }

        return channels
    }

    private fun parseExtinf(
        extinf: String,
        streamUrl: String,
        sourceUrl: String,
        forcedCountry: String?,
        index: Int
    ): Channel? {
        try {
            // Sample standard EXTINF:
            // #EXTINF:-1 tvg-id="BTV" tvg-logo="http://logo.png" group-title="News",BTV News
            // Split metadata at last comma
            val lastCommaIndex = extinf.lastIndexOf(',')
            if (lastCommaIndex == -1) return null

            val metadataBlock = extinf.substring(0, lastCommaIndex)
            val channelName = extinf.substring(lastCommaIndex + 1).trim()

            if (channelName.isEmpty() || streamUrl.isEmpty()) return null

            // Extract tvg-logo
            var logoUrl = extractAttribute(metadataBlock, "tvg-logo")
                ?: extractAttribute(metadataBlock, "logo")

            // Extract group-title
            val groupTitle = extractAttribute(metadataBlock, "group-title") ?: ""

            // Classification & Country
            var country = "Global"
            if (forcedCountry != null) {
                country = forcedCountry
            } else {
                // Infer country from sourceUrl
                if (sourceUrl.contains("/countries/bd.m3u")) {
                    country = "Bangladesh"
                } else if (sourceUrl.contains("/countries/in.m3u")) {
                    country = "India"
                } else {
                    // Try to extract from tvg-country or name matching
                    val tvgCountry = extractAttribute(metadataBlock, "tvg-country") ?: extractAttribute(metadataBlock, "country")
                    if (tvgCountry?.uppercase() == "BD" || tvgCountry?.uppercase() == "BANGLADESH") {
                        country = "Bangladesh"
                    } else if (tvgCountry?.uppercase() == "IN" || tvgCountry?.uppercase() == "INDIA") {
                        country = "India"
                    } else {
                        // Check channel name keywords for Bangladesh
                        val lowerName = channelName.lowercase()
                        if (lowerName.contains("bangla") || lowerName.contains("dhaka") || lowerName.contains("somoy") ||
                            lowerName.contains("btv") || lowerName.contains("jamuna") || lowerName.contains("independent") ||
                            lowerName.contains("ekattor") || lowerName.contains("channel i") || lowerName.contains("atn bangla")
                        ) {
                            country = "Bangladesh"
                        } else if (lowerName.contains("zee") || lowerName.contains("star plus") || lowerName.contains("sony") ||
                            lowerName.contains("india") || lowerName.contains("aaj tak") || lowerName.contains("colors")
                        ) {
                            country = "India"
                        }
                    }
                }
            }

            // Category classification
            var category = "Entertainment" // default
            val checkCategory = (groupTitle + " " + channelName).lowercase()

            if (sourceUrl.contains("/categories/sports.m3u") || checkCategory.contains("sports") || checkCategory.contains("sport") || checkCategory.contains("t-sports")) {
                category = "Sports"
            } else if (sourceUrl.contains("/categories/news.m3u") || checkCategory.contains("news") || checkCategory.contains("khabor") || checkCategory.contains("somoy") || checkCategory.contains("independent") || checkCategory.contains("jamuna") || checkCategory.contains("republic")) {
                category = "News"
            } else if (sourceUrl.contains("/categories/movies.m3u") || checkCategory.contains("movies") || checkCategory.contains("movie") || checkCategory.contains("cine") || checkCategory.contains("hbo") || checkCategory.contains("star gold")) {
                category = "Movies"
            } else if (sourceUrl.contains("/categories/music.m3u") || checkCategory.contains("music") || checkCategory.contains("songs") || checkCategory.contains("song") || checkCategory.contains("mtv")) {
                category = "Music"
            } else if (checkCategory.contains("entertainment") || checkCategory.contains("drama") || checkCategory.contains("cartoon") || checkCategory.contains("kids")) {
                category = "Entertainment"
            }

            // Infer resolution from name
            val lowerName = channelName.lowercase()
            val resolution = when {
                lowerName.contains("fhd") || lowerName.contains("1080") || lowerName.contains("1080p") -> "1080p"
                lowerName.contains("4k") || lowerName.contains("uhd") || lowerName.contains("2160") -> "4K"
                lowerName.contains("sd") || lowerName.contains("480") || lowerName.contains("360") || lowerName.contains("480p") -> "480p"
                else -> "720p" // Default HD
            }

            return Channel(
                url = streamUrl,
                name = channelName,
                logoUrl = if (logoUrl.isNullOrEmpty()) null else logoUrl,
                category = category,
                country = country,
                channelNumber = index,
                isActive = true,
                resolution = resolution
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun extractAttribute(metadata: String, attributeName: String): String? {
        val patterns = listOf(
            Regex("$attributeName\\s*=\\s*\"([^\"]*)\""),  // attribute="value"
            Regex("$attributeName\\s*=\\s*'([^']*)'"),      // attribute='value'
            Regex("$attributeName\\s*=\\s*([^\\s,]+)")        // attribute=value
        )
        for (pattern in patterns) {
            val match = pattern.find(metadata)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
}
