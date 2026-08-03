package com.arnesh.interviewsphere.repository;

import com.arnesh.interviewsphere.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arnesh.interviewsphere.annotation.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByIdAndEmail(UUID id, String email);
}