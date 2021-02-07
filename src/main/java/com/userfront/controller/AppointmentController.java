package com.userfront.controller;

import com.userfront.common.AppointmentError;
import com.userfront.domain.Appointment;
import com.userfront.domain.User;
import com.userfront.service.AppointmentService;
import com.userfront.service.UserService;
import com.userfront.validator.AppointmentValidatore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    AppointmentValidatore appointmentValidatore;

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createAppointment(Model model,Principal principal) {
        Appointment appointment = new Appointment();
        model.addAttribute("appointment", appointment);
        model.addAttribute("dateString", "");
        User user = userService.findByUsername(principal.getName());
        List<Appointment> appointmentList = this.appointmentService.findByUser(user.getUserId());
        model.addAttribute("appointmentList", appointmentList);
        return "appointment";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createAppointmentPost(@ModelAttribute("appointment") Appointment appointment,
                                        @ModelAttribute("dateString") String date,
                                        Model model, Principal principal,
                                        HttpServletRequest request) throws ParseException {

        List<String> errorList = new ArrayList<>();

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try {
            Date d1 = format1.parse(date);
            appointment.setDate(d1);
        } catch (ParseException e) {
            e.printStackTrace();
            errorList.add(AppointmentError.INVALID_DATE.getErrorMessage());
        }

        if (appointment.getLocation() == null) errorList.add(AppointmentError.INVALID_LOCATION.getErrorMessage());

        if (errorList.size() > 0) {
            request.setAttribute("err", errorList);
            return "appointment";
        }


        User user = userService.findByUsername(principal.getName());
        appointment.setUser(user);

        appointmentService.createAppointment(appointment);

        List<Appointment> appointmentList = this.appointmentService.findByUser(user.getUserId());
        model.addAttribute("appointmentList", appointmentList);
        return "appointment";
    }


}
