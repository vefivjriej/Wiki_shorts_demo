package com.example.demo.Service

import com.example.demo.Entity.UserEntity
import com.example.demo.Repository.UserRepository
import org.springframework.stereotype.Service

@Service
class RegisterService(
    private val userRepository: UserRepository
) {
    fun register(userEntity: UserEntity): UserEntity {
        if (!userRepository.existsByEmail(userEntity.email) and userRepository.existsByUserName(userEntity.userName)) {
            userRepository.save(userEntity)
        } else {
            throw RuntimeException("Такой пользователь уже существует")
        }
        return userEntity
    }
}//