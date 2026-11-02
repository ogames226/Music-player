package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VipStatus
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldDark
import com.example.ui.theme.VipGoldLight

@Composable
fun VipBadge(
    vipStatus: VipStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVip = vipStatus.isVip || vipStatus.is24HourRewardActive

    val backgroundBrush = if (isVip) {
        Brush.horizontalGradient(
            colors = listOf(VipGoldDark, VipGold, VipGoldLight)
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(DarkSurfaceVariant, DarkSurfaceVariant)
        )
    }

    val textColor by animateColorAsState(
        targetValue = if (isVip) Color(0xFF140E00) else TextSecondary,
        label = "vipTextColor"
    )

    val labelText = when {
        vipStatus.isVip -> "VIP PRO"
        vipStatus.is24HourRewardActive -> "24H VIP (${vipStatus.remainingRewardHours}h)"
        else -> "GET VIP"
    }

    Box(
        modifier = modifier
            .testTag("vip_badge_button")
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                color = if (isVip) VipGoldLight else Color(0x33FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "VIP Crown",
                tint = if (isVip) Color(0xFF140E00) else VipGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = labelText,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
