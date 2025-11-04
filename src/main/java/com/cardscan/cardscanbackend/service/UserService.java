package com.cardscan.cardscanbackend.service;

import com.cardscan.cardscanbackend.entity.User;
import com.cardscan.cardscanbackend.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findOrCreateUser(FirebaseToken decodedToken) {
        String email = decodedToken.getEmail();
        String uid = decodedToken.getUid();

        Optional<User> existingUser = userRepository.findByFirebaseUid(uid);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User userToUpdate = userByEmail.get();
            userToUpdate.setFirebaseUid(uid);
            return userRepository.save(userToUpdate);
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(decodedToken.getName());
        newUser.setPasswordHash("FIREBASE_AUTH");
        newUser.setFirebaseUid(uid);

        return userRepository.save(newUser);
    }
}