package com.userfront.validator;

import com.userfront.domain.Appointment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AppointmentValidatore implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return Appointment.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Appointment appointment = (Appointment) o;

        if(appointment.getLocation() == null)
            errors.rejectValue("location", "appointment.location.empty");

    }
}
