package com.lab.model.repository;

import com.lab.model.model.DaysOff;
import com.lab.model.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity SET user = :superiorId WHERE id = :employeeId")
    void updateSuperior(@Param("employeeId") Long employeeId, @Param("superiorId") UserEntity superiorId);

    @Modifying
    @Transactional
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE (u.user = :superiorUser OR (u.user IS NULL AND r.name IN ('AUTH', 'APPROVE_DAYS_OFF_REQUEST', 'SET_VACANCY_DAYS_NUMBER') AND SIZE(u.roles) = 3))")
    Iterable<UserEntity> findByUser(@Param("superiorUser") Optional<UserEntity> superiorUser);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity SET noDaysOff = :newNoDaysOff WHERE id = :employeeId")
    void updateNoDaysOff(@Param("employeeId") Long employeeId, @Param("newNoDaysOff") Long newNoDaysOff);

    @Query("SELECT u.noDaysOff FROM UserEntity u WHERE u = :user")
    Long getNoDaysOff(@Param("user") UserEntity user);
}
