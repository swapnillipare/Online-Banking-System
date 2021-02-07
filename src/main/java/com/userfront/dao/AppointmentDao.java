package com.userfront.dao;

import com.userfront.domain.Appointment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AppointmentDao extends CrudRepository<Appointment, Long> {

    List<Appointment> findAll();

    @Query("SELECT a from Appointment a where a.user.userId = ?1")
    List<Appointment> findByUser(long userId);
}
