package com.example.demo.Repository
import com.example.demo.Entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
    interface UserRepository: JpaRepository<UserEntity,Long>{
        fun existsByEmail(email: String): Boolean
        fun existsByUserName(username: String): Boolean
    }