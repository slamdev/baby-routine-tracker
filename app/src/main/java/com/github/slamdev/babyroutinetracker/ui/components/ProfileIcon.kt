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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.slamdev.babyroutinetracker.R
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
    onNavigateToLanguageSettings: () -> Unit,
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
                    contentDescription = stringResource(R.string.content_desc_profile_picture),
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
                            contentDescription = stringResource(R.string.content_desc_profile),
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
                        text = it.displayName ?: stringResource(R.string.default_user_name),
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
                            contentDescription = stringResource(R.string.content_desc_create_baby_profile)
                        )
                        Text(stringResource(R.string.profile_menu_create_baby))
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
                            contentDescription = stringResource(R.string.content_desc_join_profile)
                        )
                        Text(stringResource(R.string.profile_menu_join_profile))
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
                                contentDescription = stringResource(R.string.content_desc_edit_baby_profile)
                            )
                            Text(stringResource(R.string.profile_menu_edit_baby, baby.name))
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
                                contentDescription = stringResource(R.string.content_desc_notification_settings)
                            )
                            Text(stringResource(R.string.profile_menu_notification_settings))
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
                                contentDescription = stringResource(R.string.content_desc_invite_partner)
                            )
                            Text(stringResource(R.string.profile_menu_invite_partner))
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        onNavigateToInvitePartner()
                    }
                )
            }
            
            HorizontalDivider()
            
            // Language settings option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.content_desc_language_settings)
                        )
                        Text(stringResource(R.string.profile_menu_language_settings))
                    }
                },
                onClick = {
                    showDropdownMenu = false
                    onNavigateToLanguageSettings()
                }
            )
            
            // Account deletion option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.content_desc_delete_account),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.profile_menu_account_deletion),
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
                            contentDescription = stringResource(R.string.content_desc_sign_out)
                        )
                        Text(stringResource(R.string.profile_menu_sign_out))
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
