package com.talos.forge.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        error?.let { ErrorText(it) }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                showSearch = it.length >= 2
                if (showSearch) viewModel.searchUsers(it)
            },
            label = { Text("Buscar usuarios...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = Color(0xFF282828),
                unfocusedContainerColor = Color(0xFF282828)
            )
        )

        // Search results dropdown
        if (showSearch && searchResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282828))
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
                                Text(user.username, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                user.bio?.let { Text(it, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 1) }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===== Inline composer (no modal) =====
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
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
                                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF333333)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                                    Text("Video seleccionado", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        }
                        // Remove media button
                        IconButton(
                            onClick = { selectedMediaUri = null; selectedMediaType = "TEXT" },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = newPostContent,
                    onValueChange = { newPostContent = it },
                    label = { Text("¿Qué estás logrando hoy?", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.2f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF282828),
                        unfocusedContainerColor = Color(0xFF282828)
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
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo", tint = Color(0xFFA0F03C), modifier = Modifier.size(22.dp))
                        }
                        // Video button
                        IconButton(onClick = { videoPicker.launch("video/*") }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = "Video", tint = Color(0xFFA0F03C), modifier = Modifier.size(22.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0F03C)),
                        enabled = newPostContent.isNotBlank() || selectedMediaUri != null
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publicar", color = Color.White, fontSize = 13.sp)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User header
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onViewProfile() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(post.user?.username ?: "U", post.user?.profile_photo, 40.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.user?.username ?: "Usuario", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        post.user?.role?.let { role ->
                            if (role == "ADMIN" || role == "MODERATOR") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFA0F03C).copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(if (role == "ADMIN") "Admin" else "Mod", fontSize = 9.sp, color = Color(0xFFB8F56A))
                                }
                            }
                        }
                    }
                    Text(formatTimeAgo(post.created_at), fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                }
            }

            // Content
            if (post.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(post.content, fontSize = 14.sp, color = Color.White.copy(alpha = 0.95f))
            }

            // Media (photo/video)
            post.media_url?.let { url ->
                if (post.media_type == "IMAGE" || post.media_type == "PHOTO") {
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "Post media",
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Reactions bar
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Existing reactions
                val groupedReactions = post.reactions.groupBy { it.emoji }
                groupedReactions.forEach { (emoji, reacts) ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onReact(emoji) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${reacts.size}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Add reaction button
                Box {
                    IconButton(onClick = { showReactions = !showReactions }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "React", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = showReactions,
                        onDismissRequest = { showReactions = false }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            reactionEmojis.forEach { emoji ->
                                Text(
                                    emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier.clickable {
                                        onReact(emoji)
                                        showReactions = false
                                    }.padding(4.dp)
                                )
                            }
                        }
                    }
                }

                // Reply button
                Row(
                    modifier = Modifier.clickable { onReply() }.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Reply", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                    if (post.replies.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${post.replies.size}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }

            // Replies
            if (post.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                post.replies.forEach { reply ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        UserAvatar(reply.user?.username ?: "U", reply.user?.profile_photo, 24.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "${reply.user?.username ?: "Usuario"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(reply.content, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
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

            // ===== Inline reply composer =====
            if (isReplying) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Reply media preview
                        replyMediaUri?.let { uri ->
                            Box {
                                if (replyMediaType == "IMAGE") {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Reply media",
                                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF333333)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                            Text("Video", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { onReplyMediaChange(null, "TEXT") },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedTextField(
                            value = replyContent,
                            onValueChange = onReplyContentChange,
                            label = { Text("Responder a ${post.user?.username ?: "usuario"}...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedBorderColor = Color.White.copy(alpha = 0.2f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color(0xFF282828),
                                unfocusedContainerColor = Color(0xFF282828)
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(onClick = onReplyImagePick, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo", tint = Color(0xFFA0F03C), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = onReplyVideoPick, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = "Video", tint = Color(0xFFA0F03C), modifier = Modifier.size(20.dp))
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(onClick = onReplyCancel) {
                                    Text("Cancelar", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                                Button(
                                    onClick = onReplySubmit,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0F03C)),
                                    enabled = replyContent.isNotBlank() || replyMediaUri != null
                                ) {
                                    Text("Responder", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
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
        val colors = listOf(Color(0xFFA0F03C), Color(0xFFFF7043), Color(0xFFA0F03C), Color(0xFF42A5F5), Color(0xFFFFB74D))
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
        containerColor = Color(0xFF282828),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.5f))
                }
            }
        },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Profile header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(profile.user.username, profile.user.profile_photo, 56.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(profile.user.username, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            profile.user.role?.let {
                                if (it != "NORMAL") {
                                    Text(it, fontSize = 11.sp, color = Color(0xFFB8F56A))
                                }
                            }
                        }
                    }

                    profile.user.bio?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
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
                        Text("⚖️ ${it}kg", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    profile.user.height_cm?.let {
                        Text("📏 ${it}cm", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    // Recent posts
                    if (profile.posts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Publicaciones recientes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        profile.posts.take(5).forEach { post ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(post.content, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    Text(formatTimeAgo(post.created_at), fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }

                    // Routines
                    if (profile.routines.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Rutinas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        profile.routines.take(5).forEach { routine ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(routine.name, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                                routine._count?.exercises?.let {
                                    Text("$it ejercicios", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
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
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
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
