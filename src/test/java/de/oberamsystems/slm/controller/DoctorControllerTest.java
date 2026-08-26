package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DoctorControllerTest {

    @Test
    public void testDoctorController() throws Exception {
        DoctorController controller = new DoctorController();
        
        AppointmentRepository repo = mock(AppointmentRepository.class);
        DoctorRepository doctorRepo = mock(DoctorRepository.class);
        SpecialityRepository specRepo = mock(SpecialityRepository.class);
        
        java.lang.reflect.Field f1 = DoctorController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = DoctorController.class.getDeclaredField("doctorRepo");
        f2.setAccessible(true);
        f2.set(controller, doctorRepo);
        
        java.lang.reflect.Field f3 = DoctorController.class.getDeclaredField("specRepo");
        f3.setAccessible(true);
        f3.set(controller, specRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // GET /appointments
        String view = controller.addAppointments(1L, model);
        assertEquals("appointments", view);
        
        // POST /appointments
        Appointment app = new Appointment();
        String view2 = controller.submitAppointment(app, model);
        assertEquals("redirect:/appointments", view2);
        
        // GET /doctors
        String view3 = controller.addDoctors(1L, model);
        assertEquals("doctors", view3);
        
        // POST /doctors
        Doctor doc = new Doctor();
        String view4 = controller.submitDoctors(doc, model);
        assertEquals("redirect:/doctors", view4);
        
        // GET /specialities
        String view5 = controller.addSpecialities(1L, model);
        assertEquals("specialities", view5);
        
        // POST /specialities
        Speciality spec = new Speciality();
        String view6 = controller.submitSpecialities(spec, model);
        assertEquals("redirect:/specialities", view6);
        
        // GET /appointment
        when(repo.findMinDate()).thenReturn(LocalDateTime.now().minusDays(10));
        when(repo.findMaxDate()).thenReturn(LocalDateTime.now().plusDays(10));
        String view7 = controller.getSport(null, null, model);
        assertEquals("appointment", view7);
        
        String view7b = controller.getSport(LocalDateTime.now(), LocalDateTime.now(), model);
        assertEquals("appointment", view7b);

        when(repo.findMinDate()).thenReturn(null);
        when(repo.findMaxDate()).thenReturn(null);
        String view7c = controller.getSport(null, null, model);
        assertEquals("appointment", view7c);
        
        // GET /doctor
        String view8 = controller.index(model);
        assertEquals("doctor", view8);
        
        // GET /speciality
        String view9 = controller.speciality(model);
        assertEquals("speciality", view9);
        
        // GET /add-appointment
        String view10 = controller.addSport(1L, model);
        assertEquals("add-appointment", view10);
        
        // POST /add-appointment
        String view11 = controller.submitSport(app, model);
        assertEquals("add-appointment", view11);
        
        // GET /add-doctor
        String view12 = controller.addDoctor(1L, model);
        assertEquals("add-doctor", view12);
        
        // POST /add-doctor
        String view13 = controller.submitDoctor(doc, model);
        assertEquals("add-doctor", view13);
        
        // GET /add-speciality
        String view14 = controller.addSpeciality(1L, model);
        assertEquals("add-speciality", view14);
        
        // POST /add-speciality
        String view15 = controller.submitSpeciality(spec, model);
        assertEquals("add-speciality", view15);
    }
}
