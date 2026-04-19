package com.veera.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veera.core.theme.AppTheme

@Composable
 fun RecentCallItem(
    modifier: Modifier = Modifier,
    call: RecentCall,
    horizontalPadding: Dp,
    height: Dp,
    avatarSize: Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with initials
        Surface(
            modifier = Modifier.size(avatarSize),
            shape = CircleShape,
            color = if (call.isMissed) AppTheme.colors.Error.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = call.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2),
                    style = AppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (avatarSize.value * 0.35).sp,
                        color = if (call.isMissed) AppTheme.colors.Error else MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name, Number and Call Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.name,
                maxLines = 1,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = if (call.isMissed) AppTheme.colors.Error else MaterialTheme.colorScheme.onBackground
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            )
            {
                Icon(
                    imageVector = getCallTypeIcon(call.type),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (call.isMissed) AppTheme.colors.Error else AppTheme.colors.Tertiary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${call.type.name.lowercase().replaceFirstChar { it.uppercase() }} • ${call.timestamp}",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Action Button (Call Icon)
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call ${call.name}",
                tint = AppTheme.colors.Success,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


fun getCallTypeIcon(type: CallType): ImageVector {
    return when (type) {
        CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
        CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
        CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
    }
}