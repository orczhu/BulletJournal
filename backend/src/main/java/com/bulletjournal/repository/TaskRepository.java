package com.bulletjournal.repository;

import com.bulletjournal.repository.models.Project;
import com.bulletjournal.repository.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, TaskRepositoryCustom {
    List<Task> findTaskByProject(Project project);

    @Query(value = "SELECT * FROM tasks WHERE :assignee = ANY(tasks.assignees) AND tasks.recurrence_rule IS NOT NULL", nativeQuery = true)
    List<Task> findTasksByAssigneesAndRecurrenceRuleNotNull(@Param("assignee") String assignee);

    Optional<Task> findTaskByGoogleCalendarEventId(String googleCalendarEventId);

    @Query(value = "SELECT * FROM tasks WHERE :assignee = ANY(tasks.assignees) AND tasks.start_time IS NOT NULL AND tasks.reminder_date_time IS NOT NULL" +
            " AND tasks.start_time >= to_timestamp(:now, 'YYYY-MM-DD HH24:MI:SS') AND tasks.reminder_date_time <= to_timestamp(:now, 'YYYY-MM-DD HH24:MI:SS')", nativeQuery = true)
    List<Task> findRemindingTasks(@Param("assignee") String assignee, @Param("now") String now);

    @Query(value = "SELECT * FROM tasks WHERE :assignee = ANY(tasks.assignees) AND tasks.start_time IS NOT NULL AND " +
            "tasks.end_time IS NOT NULL AND " +
            "((tasks.start_time >= to_timestamp(:startTime, 'YYYY-MM-DD HH24:MI:SS') AND tasks.start_time <= to_timestamp(:endTime, 'YYYY-MM-DD HH24:MI:SS')) OR " +
            "(tasks.end_time >= to_timestamp(:startTime, 'YYYY-MM-DD HH24:MI:SS') AND tasks.end_time <= to_timestamp(:endTime, 'YYYY-MM-DD HH24:MI:SS')))", nativeQuery = true)
    List<Task> findTasksOfAssigneeBetween(@Param("assignee") String assignee,
                                          @Param("startTime") String startTime,
                                          @Param("endTime") String endTime);
}
