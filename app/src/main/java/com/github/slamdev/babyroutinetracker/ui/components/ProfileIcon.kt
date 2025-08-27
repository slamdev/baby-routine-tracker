package com.github.slamdev.babyroutinetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.slamdev.babyroutinetracker.model.Baby
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileIcon(
    user: FirebaseUser?,
    onSignOut: () -> Unit,
    babies: List<Baby>,
    selectedBaby: Baby?,
    onNavigateToCreateBaby: () -> Unit,
    onNavigateToJoinInvitation: () -> Unit,
    onNavigateToInvitePartner: () -> Unit,
    onNavigateToEditBaby: (Baby) -> Unit,
    onNavigateToNotificationSettings: (Baby) -> Unit,
    onNavigateToAccountDeletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    Box {
        // Profile icon/avatar
        Box(
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { showDropdownMenu = true },
            contentAlignment = Alignment.Center
        ) {
            if (user?.photoUrl != null) {
                // Load Google profile picture
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    fallback = null,
                    error = null
                )
            } else {
                // Fallback avatar with user's initial or person icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.displayName?.isNotEmpty() == true) {
                        Text(
                            text = user.displayName!!.firstOrNull()?.toString()?.uppercase() ?: "U",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = { showDropdownMenu = false }
        ) {
            // User info
            user?.let {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it.displayName ?: "User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (it.email != null) {
                        Text(
                            text = it.email!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                HorizontalDivider()
            }
            
            // Baby Profile Management Options
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Baby Profile"
                        )
                        Text("Create Baby Profile")
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onNavigateToCreateBaby()
                }
            )
            
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Join Profile"
                        )
                        Text("Join Profile")
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onNavigateToJoinInvitation()
                }
            )

            // Show edit baby profile option if a baby is selected
            selectedBaby?.let { baby ->
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person, // Using Person for edit as Edit icon may not be available
                                contentDescription = "Edit Baby Profile"
                            )
                            Text("Edit ${baby.name}")
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        onNavigateToEditBaby(baby)
                    }
                )
                
                // Notification settings for selected baby
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notification Settings"
                            )
                            Text("Notifications")
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        onNavigateToNotificationSettings(baby)
                    }
                )
            }
            
            // Only show invite partner option if there are baby profiles
            if (babies.isNotEmpty()) {
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Invite Partner"
                            )
                            Text("Invite Partner")
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        onNavigateToInvitePartner()
                    }
                )
            }
            
            HorizontalDivider()
            
            // Account deletion option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Account",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Delete Account",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onNavigateToAccountDeletion()
                }
            )
            
            HorizontalDivider()
            
            // Sign out option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                        Text("Sign Out")
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onSignOut()
                }
            )
        }
    }
}
