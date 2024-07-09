package com.lab.model.repository;

import com.lab.model.model.DaysOff;
import com.lab.model.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DaysOffRepository extends CrudRepository<DaysOff, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE DaysOff d SET d.isApproved = :isApproved, d.message = :message WHERE d.id = :daysOffId")
    void updateStatusAndMessageById(@Param("daysOffId") Long daysOffId,
                                    @Param("isApproved") Boolean isApproved,
                                    @Param("message") String message);

    @Modifying
    @Transactional
    @Query("UPDATE DaysOff d SET d.isApproved = :status WHERE d.id = :daysOffId")
    void updateStatusById(Long daysOffId, Boolean status);

    Iterable<DaysOff> findByUser(Optional<UserEntity> user);

    @Query("SELECT d FROM DaysOff d JOIN d.user u JOIN u.roles r  WHERE (u.user = :superiorUser OR (u.user IS NULL AND r.name IN ('AUTH', 'APPROVE_DAYS_OFF_REQUEST', 'SET_VACANCY_DAYS_NUMBER') AND SIZE(u.roles) = 3))")
    Iterable<DaysOff> findByUser_User(@Param("superiorUser") Optional<UserEntity> superiorUser);
}
