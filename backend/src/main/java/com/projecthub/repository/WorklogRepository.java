package com.projecthub.repository;

import com.projecthub.entity.Worklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorklogRepository extends JpaRepository<Worklog, UUID> {

    List<Worklog> findByTaskId(UUID taskId);

    List<Worklog> findByUserId(UUID userId);

    List<Worklog> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

    List<Worklog> findByUserIdAndWorkDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    List<Worklog> findByTaskIdAndWorkDateBetween(UUID taskId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(w.hours) FROM Worklog w WHERE w.task.id = :taskId")
    BigDecimal sumHoursByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT SUM(w.hours) FROM Worklog w WHERE w.user.id = :userId AND w.workDate BETWEEN :startDate AND :endDate")
    BigDecimal sumHoursByUserIdAndDateRange(@Param("userId") UUID userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(w.hours) FROM Worklog w WHERE w.workDate BETWEEN :startDate AND :endDate")
    BigDecimal sumHoursByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    long countByTaskId(UUID taskId);
}
