package com.example.demo.Entity

import jakarta.persistence.*

@Entity
@Table(name="userszahar")
data class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userName:String,
    val email:String,
    val password:String
)//