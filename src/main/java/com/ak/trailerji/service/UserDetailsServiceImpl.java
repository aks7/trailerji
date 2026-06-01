package com.ak.trailerji.service;

import com.ak.trailerji.entity.User;
import com.ak.trailerji.repository.UserRepository;
import com.ak.trailerji.security.UserDetailsImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Loads the user by username (or email, based on your app logic)
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by username
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User Not Found with username or email: " + usernameOrEmail)));

        // Return a UserDetails object (could use a custom implementation)



        return UserDetailsImpl.build(user);
    }

    public void saveUser(User user) {
        userRepository.save(user);
        entityManager.persist(user);
    }
}
