package com.nammasanthe.ledger.ui.components.fintech

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.ui.theme.BackgroundCream
import com.nammasanthe.ledger.ui.theme.CreditPinkSoft
import com.nammasanthe.ledger.ui.theme.Dimens
import com.nammasanthe.ledger.ui.theme.NavBarBackground
import com.nammasanthe.ledger.ui.theme.NavPillSelected
import com.nammasanthe.ledger.ui.theme.OutstandingGradient
import com.nammasanthe.ledger.ui.theme.PaidGreen
import com.nammasanthe.ledger.ui.theme.PaymentGreenSoft
import com.nammasanthe.ledger.ui.theme.PendingRed
import com.nammasanthe.ledger.ui.theme.PrimaryOrange
import com.nammasanthe.ledger.ui.theme.SurfaceWhite
import com.nammasanthe.ledger.ui.theme.TextOnGradient
import com.nammasanthe.ledger.ui.theme.TextSecondary

@Composable
fun FintechScreenBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

@Composable
fun HomeWelcomeHeader(
    welcomeText: String,
    businessName: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = welcomeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = businessName.ifBlank { "—" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            onClick = onSettingsClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = PrimaryOrange)
            }
        }
    }
}

@Composable
fun OutstandingGradientCard(
    label: String,
    amount: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(Dimens.cardRadiusLarge), ambientColor = PrimaryOrange.copy(0.2f)),
        shape = RoundedCornerShape(Dimens.cardRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OutstandingGradient)
                .padding(Dimens.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = TextOnGradient.copy(0.9f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        amount,
                        style = MaterialTheme.typography.displayLarge,
                        color = TextOnGradient,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnGradient.copy(0.85f)
                    )
                }
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = TextOnGradient.copy(0.7f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    amount: String,
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(Dimens.cardRadius), ambientColor = Color.Black.copy(0.06f)),
        shape = RoundedCornerShape(Dimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = iconTint.copy(0.15f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun FintechTransactionRow(
    customerName: String,
    dateLabel: String,
    amount: Int,
    type: TransactionType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val isCredit = type == TransactionType.CREDIT
    val amountColor = if (isCredit) PendingRed else PaidGreen
    val iconBg = if (isCredit) CreditPinkSoft else PaymentGreenSoft
    val icon = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(0.04f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerAvatar(customerName, iconBg, icon, iconTint = amountColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(dateLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                trailing?.invoke()
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹$amount",
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun CustomerAvatar(
    name: String,
    backgroundColor: Color = PrimaryOrange.copy(0.12f),
    overlayIcon: ImageVector? = null,
    iconTint: Color = PrimaryOrange,
    modifier: Modifier = Modifier
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(Dimens.avatarSize)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (overlayIcon != null) {
            Icon(overlayIcon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        } else {
            Text(
                initial,
                style = MaterialTheme.typography.titleLarge,
                color = iconTint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CustomerListCard(
    name: String,
    balance: Int,
    hasPhone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balanceColor = if (balance > 0) PendingRed else PaidGreen
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerAvatar(name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (hasPhone) {
                    Text("•", style = MaterialTheme.typography.labelSmall, color = PaidGreen)
                }
            }
            Text(
                "₹$balance",
                style = MaterialTheme.typography.titleMedium,
                color = balanceColor,
                fontWeight = FontWeight.SemiBold
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun FintechSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.chipRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun FintechFilterChips(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelected(index) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavPillSelected,
                    selectedLabelColor = PrimaryOrange
                ),
                shape = RoundedCornerShape(Dimens.chipRadius)
            )
        }
    }
}

@Composable
fun FintechFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.92f else 1f,
        animationSpec = tween(120),
        label = "fabScale"
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .size(Dimens.fabSize)
            .shadow(12.dp, CircleShape, ambientColor = PrimaryOrange.copy(0.35f)),
        shape = CircleShape,
        color = PrimaryOrange,
        interactionSource = interaction
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun FintechBottomBar(
    tabs: List<Pair<String, ImageVector>>,
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    tabLabel: @Composable (String) -> String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSystemInDarkTheme()) Color(0xFF1C1B1F) else Color.White
    val selectedBg = if (isSystemInDarkTheme()) Color(0xFF45271D) else NavPillSelected

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shadowElevation = 16.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (route, icon) ->
                val selected = selectedRoute == route
                val label = tabLabel(route)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) selectedBg else Color.Transparent)
                        .clickable { onTabSelected(route) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = if (selected) PrimaryOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) PrimaryOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(4.dp)) { content() }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Surface(shape = CircleShape, color = PrimaryOrange.copy(0.1f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = PrimaryOrange, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmptyStateMessage(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
