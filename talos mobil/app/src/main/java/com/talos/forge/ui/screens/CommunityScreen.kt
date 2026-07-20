package com.talos.forge.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.talos.forge.data.models.CommunityPost
import com.talos.forge.data.models.CommunityUser
import com.talos.forge.ui.CommunityViewModel
import com.talos.forge.ui.components.ErrorText
import com.talos.forge.ui.components.LoadingSpinner
import com.talos.forge.ui.theme.AppColors
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun CommunityScreen(viewModel: CommunityViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoadingProfile by viewModel.isLoadingProfile.collectAsState()
    val context = LocalContext.current

    var newPostContent by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var replyingToId by remember { mutableStateOf<String?>(null) }
    var replyContent by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMediaType by remember { mutableStateOf("TEXT") }
    var replyMediaUri by remember { mutableStateOf<Uri?>(null) }
    var replyMediaType by remember { mutableStateOf("TEXT") }

    LaunchedEffect(Unit) { viewModel.loadFeed() }

    LaunchedEffect(error) {
        if (error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    // Media pickers for new post
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            selectedMediaType = "IMAGE"
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            selectedMediaType = "VIDEO"
        }
    }

    // Media pickers for replies
    val replyImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            replyMediaUri = uri
            replyMediaType = "IMAGE"
        }
    }

    val replyVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            replyMediaUri = uri
            replyMediaType = "VIDEO"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Comunidad", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AppColors.textPrimary)
                Text("Comparte tu progreso", fontSize = 12.sp, color = AppColors.textSecondary)
            }
            IconButton(
                onClick = { showSearch = !showSearch },
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.accentMuted)
            ) {
                Icon(
                    if (showSearch) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = AppColors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Search bar (collapsible)
        AnimatedVisibility(visible = showSearch) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.length >= 2) viewModel.searchUsers(it)
                    },
                    label = { Text("Buscar usuarios...", color = AppColors.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.accent,
                        focusedBorderColor = AppColors.accent,
                        unfocusedBorderColor = AppColors.border,
                        focusedContainerColor = AppColors.cardBgAlt,
                        unfocusedContainerColor = AppColors.cardBgAlt
                    )
                )

                // Search results dropdown
                if (searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBgAlt)
                    ) {
                        Column {
                            searchResults.take(5).forEach { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.loadUserProfile(user.id)
                                            showSearch = false
                                            searchQuery = ""
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(user.username, user.profile_photo, 32.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.username, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                                        user.bio?.let { Text(it, fontSize = 11.sp, color = AppColors.textSecondary, maxLines = 1) }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        error?.let { ErrorText(it) }

        Spacer(modifier = Modifier.height(4.dp))

        // ===== Inline composer (no modal) =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Media preview
                selectedMediaUri?.let { uri ->
                    Box {
                        if (selectedMediaType == "IMAGE") {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected media",
                                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.cardBgAlt),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = AppColors.textPrimary, modifier = Modifier.size(40.dp))
                                    Text("Video seleccionado", fontSize = 12.sp, color = AppColors.textSecondary)
                                }
                            }
                        }
                        // Remove media button
                        IconButton(
                            onClick = { selectedMediaUri = null; selectedMediaType = "TEXT" },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = AppColors.textPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = newPostContent,
                    onValueChange = { newPostContent = it },
                    label = { Text("¿Qué estás logrando hoy?", color = AppColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        cursorColor = AppColors.accent,
                        focusedBorderColor = AppColors.border,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = AppColors.cardBgAlt,
                        unfocusedContainerColor = AppColors.cardBgAlt
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Photo button
                        IconButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo", tint = AppColors.accent, modifier = Modifier.size(22.dp))
                        }
                        // Video button
                        IconButton(onClick = { videoPicker.launch("video/*") }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = "Video", tint = AppColors.accent, modifier = Modifier.size(22.dp))
                        }
                    }

                    Button(
                        onClick = {
                            if (newPostContent.isNotBlank() || selectedMediaUri != null) {
                                val mediaBase64 = selectedMediaUri?.let { uriToBase64(context, it) }
                                viewModel.createPost(newPostContent, mediaBase64, selectedMediaType)
                                newPostContent = ""
                                selectedMediaUri = null
                                selectedMediaType = "TEXT"
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent),
                        enabled = newPostContent.isNotBlank() || selectedMediaUri != null
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = AppColors.textOnAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publicar", color = AppColors.textOnAccent, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading && posts.isEmpty()) {
            LoadingSpinner()
            return@Column
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(posts) { post ->
                PostCard(
                    post = post,
                    isReplying = replyingToId == post.id,
                    replyContent = replyContent,
                    replyMediaUri = replyMediaUri,
                    replyMediaType = replyMediaType,
                    onReplyContentChange = { replyContent = it },
                    onReplyMediaChange = { uri, type -> replyMediaUri = uri; replyMediaType = type },
                    onReplyImagePick = { replyImagePicker.launch("image/*") },
                    onReplyVideoPick = { replyVideoPicker.launch("video/*") },
                    onReplySubmit = {
                        val mediaBase64 = replyMediaUri?.let { uriToBase64(context, it) }
                        viewModel.replyToPost(post.id, replyContent, mediaBase64, replyMediaType)
                        replyContent = ""
                        replyMediaUri = null
                        replyMediaType = "TEXT"
                        replyingToId = null
                    },
                    onReplyCancel = {
                        replyingToId = null
                        replyContent = ""
                        replyMediaUri = null
                        replyMediaType = "TEXT"
                    },
                    onReact = { emoji -> viewModel.reactToPost(post.id, emoji) },
                    onReply = { replyingToId = if (replyingToId == post.id) null else post.id },
                    onViewProfile = { post.user?.let { viewModel.loadUserProfile(it.id) } },
                    onDelete = { viewModel.deletePost(post.id) }
                )
            }
        }
    }

    // User profile dialog
    userProfile?.let { profile ->
        UserProfileDialog(
            profile = profile,
            isLoading = isLoadingProfile,
            onDismiss = { viewModel.clearUserProfile() }
        )
    }
}

private fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        inputStream.close()
        val base64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
        "data:image/jpeg;base64,$base64"
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun PostCard(
    post: CommunityPost,
    isReplying: Boolean,
    replyContent: String,
    replyMediaUri: Uri?,
    replyMediaType: String,
    onReplyContentChange: (String) -> Unit,
    onReplyMediaChange: (Uri?, String) -> Unit,
    onReplyImagePick: () -> Unit,
    onReplyVideoPick: () -> Unit,
    onReplySubmit: () -> Unit,
    onReplyCancel: () -> Unit,
    onReact: (String) -> Unit,
    onReply: () -> Unit,
    onViewProfile: () -> Unit,
    onDelete: () -> Unit
) {
    val reactionEmojis = listOf("🔥", "💪", "❤️", "👏", "🚀")
    var showReactions by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }

    val totalReactions = post.reactions.size
    val topReactions = post.reactions.groupBy { it.emoji }.entries.sortedByDescending { it.value.size }.take(3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // ===== Header: avatar + username + time + menu =====
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(post.user?.username ?: "U", post.user?.profile_photo, 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f).clickable { onViewProfile() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.user?.username ?: "Usuario", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        post.user?.role?.let { role ->
                            if (role == "ADMIN" || role == "MODERATOR") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(AppColors.accentMuted)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(if (role == "ADMIN") "Admin" else "Mod", fontSize = 9.sp, color = AppColors.accent)
                                }
                            }
                        }
                    }
                    Text(formatTimeAgo(post.created_at), fontSize = 11.sp, color = AppColors.textSecondary)
                }
                Box {
                    IconButton(onClick = { showMenu = !showMenu }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "Options", tint = AppColors.textTertiary, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver perfil", fontSize = 13.sp, color = AppColors.textPrimary) },
                            onClick = { showMenu = false; onViewProfile() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", fontSize = 13.sp, color = AppColors.danger) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            // ===== Media full-width (Instagram style) =====
            post.media_url?.let { url ->
                if (post.media_type == "IMAGE" || post.media_type == "PHOTO") {
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Post media",
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f).pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { onReact("❤️") }
                                )
                            },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // ===== Action bar (Instagram style) =====
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like / reactions button
                Box {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showReactions = !showReactions },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (liked) AppColors.danger else AppColors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        // Reaction emojis quick bar
                        AnimatedVisibility(
                            visible = showReactions,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                reactionEmojis.forEach { emoji ->
                                    Text(
                                        emoji,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            onReact(emoji)
                                            liked = true
                                            showReactions = false
                                        }.padding(2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Comment button
                IconButton(
                    onClick = { showComments = !showComments; if (!showComments) onReply() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", tint = AppColors.textPrimary, modifier = Modifier.size(22.dp))
                }
                if (post.replies.isNotEmpty()) {
                    Text("${post.replies.size}", fontSize = 12.sp, color = AppColors.textSecondary)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Share button (visual only)
                IconButton(onClick = { /* share intent */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Send, contentDescription = "Share", tint = AppColors.textPrimary, modifier = Modifier.size(20.dp))
                }
            }

            // ===== Likes count =====
            if (totalReactions > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show top reaction emojis
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        topReactions.forEach { (emoji, _) ->
                            Text(emoji, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (totalReactions == 1) "1 reacción" else "$totalReactions reacciones",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }
            }

            // ===== Caption =====
            if (post.content.isNotBlank()) {
                Text(
                    post.content,
                    fontSize = 13.sp,
                    color = AppColors.textPrimary,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ===== Comments preview (first 2) =====
            if (post.replies.isNotEmpty() && !showComments) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    Text(
                        "Ver los ${post.replies.size} comentarios",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary,
                        modifier = Modifier.clickable { showComments = true; onReply() }.padding(vertical = 4.dp)
                    )
                    post.replies.take(2).forEach { reply ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                "${reply.user?.username ?: "Usuario"} ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary
                            )
                            Text(reply.content, fontSize = 12.sp, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // ===== Full comments section =====
            if (showComments && post.replies.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                    post.replies.forEach { reply ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            UserAvatar(reply.user?.username ?: "U", reply.user?.profile_photo, 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        reply.user?.username ?: "Usuario",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(formatTimeAgo(reply.created_at), fontSize = 10.sp, color = AppColors.textTertiary)
                                }
                                Text(reply.content, fontSize = 13.sp, color = AppColors.textPrimary)
                                reply.media_url?.let { url ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Reply media",
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ===== Inline reply composer =====
            if (isReplying) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBgAlt)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        replyMediaUri?.let { uri ->
                            Box {
                                if (replyMediaType == "IMAGE") {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Reply media",
                                        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.cardBgSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = AppColors.textPrimary, modifier = Modifier.size(28.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { onReplyMediaChange(null, "TEXT") },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = AppColors.textPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedTextField(
                            value = replyContent,
                            onValueChange = onReplyContentChange,
                            label = { Text("Comentar...", color = AppColors.textSecondary, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AppColors.textPrimary,
                                unfocusedTextColor = AppColors.textPrimary,
                                cursorColor = AppColors.accent,
                                focusedBorderColor = AppColors.border,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = AppColors.cardBgSubtle,
                                unfocusedContainerColor = AppColors.cardBgSubtle
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(onClick = onReplyImagePick, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo", tint = AppColors.accent, modifier = Modifier.size(18.dp))
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = onReplyCancel) {
                                    Text("Cancelar", color = AppColors.textSecondary, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onReplySubmit,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent),
                                    enabled = replyContent.isNotBlank() || replyMediaUri != null
                                ) {
                                    Text("Comentar", color = AppColors.textOnAccent, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun UserAvatar(username: String, photoUrl: String?, size: androidx.compose.ui.unit.Dp) {
    if (photoUrl != null) {
        AsyncImage(
            model = photoUrl,
            contentDescription = username,
            modifier = Modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val colors = listOf(AppColors.accent, AppColors.accentDark, AppColors.textSecondary, AppColors.textTertiary, AppColors.accent)
        val colorIndex = username.firstOrNull()?.hashCode()?.rem(colors.size)?.let { if (it < 0) it + colors.size else it } ?: 0
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(colors[colorIndex].copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(username.take(1).uppercase(), fontSize = (size.value * 0.4f).sp, fontWeight = FontWeight.Bold, color = colors[colorIndex])
        }
    }
}

@Composable
private fun UserProfileDialog(
    profile: com.talos.forge.data.models.CommunityUserProfileResponse,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Perfil", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                }
            }
        },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Profile header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(profile.user.username, profile.user.profile_photo, 56.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(profile.user.username, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            profile.user.role?.let {
                                if (it != "NORMAL") {
                                    Text(it, fontSize = 11.sp, color = AppColors.accent)
                                }
                            }
                        }
                    }

                    profile.user.bio?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, fontSize = 13.sp, color = AppColors.textSecondary)
                    }

                    // Stats
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStat("${profile.user.post_count}", "Posts")
                        ProfileStat("${profile.user.routine_count}", "Rutinas")
                        profile.user.goal?.let { ProfileStat(it, "Objetivo") }
                    }

                    // Physical info
                    profile.user.weight_kg?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⚖️ ${it}kg", fontSize = 12.sp, color = AppColors.textSecondary)
                    }
                    profile.user.height_cm?.let {
                        Text("� ${it}cm", fontSize = 12.sp, color = AppColors.textSecondary)
                    }

                    // Recent posts
                    if (profile.posts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Publicaciones recientes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        profile.posts.take(5).forEach { post ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBgSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(post.content, fontSize = 13.sp, color = AppColors.textPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Text(formatTimeAgo(post.created_at), fontSize = 10.sp, color = AppColors.textTertiary)
                                }
                            }
                        }
                    }

                    // Routines
                    if (profile.routines.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Rutinas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        profile.routines.take(5).forEach { routine ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(routine.name, fontSize = 13.sp, color = AppColors.textSecondary)
                                routine._count?.exercises?.let {
                                    Text("$it ejercicios", fontSize = 11.sp, color = AppColors.textTertiary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
    }
}

private fun formatTimeAgo(dateString: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateString)
        if (date != null) {
            val diff = System.currentTimeMillis() - date.time
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24
            when {
                days > 0 -> "hace ${days}d"
                hours > 0 -> "hace ${hours}h"
                minutes > 0 -> "hace ${minutes}min"
                else -> "ahora"
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}
