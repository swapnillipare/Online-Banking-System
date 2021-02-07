package com.userfront.service;

import com.userfront.domain.Appointment;

import java.util.List;

public interface AppointmentService {
	Appointment createAppointment(Appointment appointment);

    List<Appointment> findAll();

    Appointment findAppointment(Long id);

    List<Appointment> findByUser(long userId);

    void confirmAppointment(Long id);
}
