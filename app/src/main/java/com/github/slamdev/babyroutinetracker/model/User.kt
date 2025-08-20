package com.github.slamdev.babyroutinetracker.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val lastActiveAt: Timestamp = Timestamp.now()
)
