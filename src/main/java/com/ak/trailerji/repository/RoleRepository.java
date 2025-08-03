package com.ak.trailerji.repository;

import com.ak.trailerji.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name); // If your Role entity uses a String name
    // or Optional<Role> findByName(ERole name); if you use an enum
}
