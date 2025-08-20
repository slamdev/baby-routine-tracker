package com.github.slamdev.babyroutinetracker.model

import com.google.firebase.Timestamp

data class Baby(
    val id: String = "",
    val name: String = "",
    val birthDate: Timestamp = Timestamp.now(),
    val parentIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
