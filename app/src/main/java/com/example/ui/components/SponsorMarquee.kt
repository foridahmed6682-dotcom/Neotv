package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sponsor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SponsorMarquee(
    sponsors: List<Sponsor>,
    modifier: Modifier = Modifier
) {
    val scrollMessage = if (sponsors.isEmpty()) {
        "WELCOME TO SLATE IPTV • LIVE STREAM HYBRID IPTV PORTAL • SPEED RANKED & FULLY AUTOMATIC • ENJOY STREAMING!"
    } else {
        sponsors.joinToString("   ★   ") { it.description.uppercase() }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Styled Sponsor Label Badge
        Box(
            modifier = Modifier
                .background(Color(0xFF1E293B), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "NOTICE:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Auto-Scrolling Text Marquee
        Text(
            text = scrollMessage,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    initialDelayMillis = 1000
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Small pulse indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFF22C55E), RoundedCornerShape(50))
        )
    }
}
