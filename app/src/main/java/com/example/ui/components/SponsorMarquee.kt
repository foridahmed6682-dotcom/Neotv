package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Sponsor
import kotlinx.coroutines.delay

@Composable
fun SponsorMarquee(
    sponsors: List<Sponsor>,
    modifier: Modifier = Modifier
) {
    if (sponsors.isEmpty()) return

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isPausedByInteraction by remember { mutableStateOf(false) }

    // Double the sponsors list to make infinite looping scroll clean & seamless
    val items = remember(sponsors) { sponsors + sponsors + sponsors }

    // Infinite programmatic scrolling effect
    LaunchedEffect(isPausedByInteraction, items) {
        if (!isPausedByInteraction && items.isNotEmpty()) {
            while (true) {
                val current = scrollState.value
                val max = scrollState.maxValue
                if (max > 0) {
                    if (current >= max - 2) {
                        scrollState.scrollTo(0)
                    } else {
                        scrollState.scrollBy(2.2f) // Optimized speed for smooth sliding ticker
                    }
                }
                delay(16) // roughly 60Hz update rate
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1E293B).copy(alpha = 0.5f)) // Premium Dark Slate overlay
            .border(
                border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        // Continuous horizontal scroll row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState, enabled = false) // Disable touch drag override to maintain smooth continuous sliding
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val type = event.type
                            // Pause on touch pointer drag or enter
                            if (type == androidx.compose.ui.input.pointer.PointerEventType.Press ||
                                type == androidx.compose.ui.input.pointer.PointerEventType.Enter) {
                                isPausedByInteraction = true
                            } else if (type == androidx.compose.ui.input.pointer.PointerEventType.Release ||
                                       type == androidx.compose.ui.input.pointer.PointerEventType.Exit) {
                                isPausedByInteraction = false
                            }
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, sponsor ->
                SponsorCardItem(
                    sponsor = sponsor,
                    index = index,
                    onFocused = { focused ->
                        if (focused) isPausedByInteraction = true
                    },
                    onUnfocused = {
                        isPausedByInteraction = false
                    },
                    onClick = {
                        launchBrowser(context, sponsor.linkUrl)
                    }
                )
                // Spacer between sponsors marquee
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), shape = CircleShape)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        // Overlay gradients on sides for high-end soft fade edges
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(20.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0F172A), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(20.dp)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF0F172A))
                    )
                )
        )
    }
}

@Composable
fun SponsorCardItem(
    sponsor: Sponsor,
    index: Int,
    onFocused: (Boolean) -> Unit,
    onUnfocused: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isFocused) Color(0xFF334155) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current
            ) {
                onClick()
            }
            .onFocusChanged {
                isFocused = it.isFocused
                onFocused(it.isFocused)
            }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("sponsor_marquee_item_$index"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sponsor Corporate Logo
        if (sponsor.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = sponsor.imageUrl,
                contentDescription = "Sponsor Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E293B))
            )
            SpacerWidth(8)
        }

        // Sponsor Tagline
        Text(
            text = "SPONSOR: ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        )

        Text(
            text = sponsor.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun launchBrowser(context: Context, url: String) {
    try {
        var cleanUrl = url.trim()
        if (cleanUrl.isNotEmpty()) {
            if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                cleanUrl = "https://$cleanUrl"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        Log.e("SponsorMarquee", "Error launching external browser for link: $url", e)
    }
}
