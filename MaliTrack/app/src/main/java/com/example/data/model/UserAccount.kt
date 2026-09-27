package com.example.data.model

data class UserAccount(
  val id: String,
  val fullName: String,
  val emailOrPhone: String,
  val passwordHash: String,
  val createdAt: Long = System.currentTimeMillis()
)
