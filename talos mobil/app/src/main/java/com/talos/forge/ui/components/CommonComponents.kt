package com.talos.forge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talos.forge.ui.theme.AppColors

@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = AppColors.accent,
            strokeWidth = 2.dp,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ErrorText(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.danger.copy(alpha = 0.15f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = AppColors.danger,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EmptyState(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = icon, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun TalosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = if (isPrimary) ButtonDefaults.buttonColors(
            containerColor = AppColors.accent,
            contentColor = Color.White,
            disabledContainerColor = AppColors.accent.copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ) else ButtonDefaults.buttonColors(
            containerColor = AppColors.cardBg,
            contentColor = AppColors.textPrimary
        )
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
