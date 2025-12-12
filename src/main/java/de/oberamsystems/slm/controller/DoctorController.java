package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Appointment;
import de.oberamsystems.slm.model.AppointmentRepository;
import de.oberamsystems.slm.model.Doctor;
import de.oberamsystems.slm.model.DoctorRepository;
import de.oberamsystems.slm.model.Speciality;
import de.oberamsystems.slm.model.SpecialityRepository;

@Controller
public class DoctorController {

	Logger log = LoggerFactory.getLogger(DoctorController.class);
	
	@Autowired
	private AppointmentRepository repo;
	
	@Autowired
	private DoctorRepository doctorRepo;
	
	@Autowired
	private SpecialityRepository specRepo;
	
	@GetMapping("/appointments")
	public String addAppointments(@RequestParam(required = false) Long id, Model model) {
		Appointment a = new Appointment();
		a.setStart(LocalDateTime.now());
		a.setEnd(LocalDateTime.now());
		a.setDeparture(LocalDateTime.now());
		a.setArrival(LocalDateTime.now());
		a.setArrivalHome(LocalDateTime.now());
		model.addAttribute("appointment", a);
		model.addAttribute("doctors", doctorRepo.findAll());
		model.addAttribute("appointments", repo.findAll());
		return "appointments";
	}
	
	@PostMapping("/appointments")
	public String submitAppointment(@ModelAttribute Appointment app, Model model) {
		model.addAttribute("appointments", repo.findAll());
		model.addAttribute("doctors", doctorRepo.findAll());
		repo.save(app);
		model.addAttribute("appointment", app);
		return "redirect:/appointments";
	}
	
	@GetMapping("/doctors")
	public String addDoctors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("specialities", specRepo.findAll());
		model.addAttribute("doctors", doctorRepo.findAll());
		model.addAttribute("doctor", new Doctor());
		return "doctors";
	}
	
	@PostMapping("/doctors")
	public String submitDoctors(@ModelAttribute Doctor doc, Model model) {
		model.addAttribute("specialities", specRepo.findAll());
		model.addAttribute("doctors", doctorRepo.findAll());
		doctorRepo.save(doc);
		model.addAttribute("doctor", doc);
		return "redirect:/doctors";
	}

	@GetMapping("/specialities")
	public String addSpecialities(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("speciality", new Speciality());
		model.addAttribute("specialities", specRepo.findAll());
		return "specialities";
	}

	@PostMapping("/specialities")
	public String submitSpecialities(@ModelAttribute Speciality spec, Model model) {
		specRepo.save(spec);
		model.addAttribute("speciality", spec);
		model.addAttribute("specialities", specRepo.findAll());
		return "redirect:/specialities";
	}

	@GetMapping("/appointment")
	public String getSport(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusDays(90) : fromDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		LocalDateTime minDate = repo.findMinDate();
	    LocalDateTime maxDate = repo.findMaxDate();

	    minDate = minDate == null ? LocalDateTime.now().minusDays(90) : minDate.minusDays(1); //fixes off by one bug
	    maxDate = maxDate == null ? LocalDateTime.now(): maxDate;

		model.addAttribute("appointments", repo.findByStartBetween(fromDate, toDate.plusDays(1), Sort.by("start"))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "appointment";
	}
	
	@GetMapping({"/doctor.html", "/doctor"})
	public String index(Model model) {
		List<Doctor> docs = doctorRepo.findAll();
		model.addAttribute("doctors", docs);
		return "doctor";
	}
	
	@GetMapping({"/speciality.html", "/speciality"})
	public String speciality(Model model) {
		List<Speciality> specs = specRepo.findAll();
		model.addAttribute("specialities", specs);
		return "speciality";
	}
	
	@GetMapping("/add-appointment")
	public String addSport(@RequestParam(required = false) Long id, Model model) {
		Appointment a = new Appointment();
		a.setStart(LocalDateTime.now());
		a.setEnd(LocalDateTime.now());
		a.setDeparture(LocalDateTime.now());
		a.setArrival(LocalDateTime.now());
		a.setArrivalHome(LocalDateTime.now());
		model.addAttribute("appointment", a);
		model.addAttribute("doctors", doctorRepo.findAll());
		return "add-appointment";
	}
	
	@PostMapping("/add-appointment")
	public String submitSport(@ModelAttribute Appointment app, Model model) {
		model.addAttribute("appointments", doctorRepo.findAll());
		repo.save(app);
		model.addAttribute("appointment", app);
		return "add-appointment";
	}
	
	@GetMapping("/add-doctor")
	public String addDoctor(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("specialities", specRepo.findAll());
		model.addAttribute("doctor", new Doctor());
		return "add-doctor";
	}
	
	@PostMapping("/add-doctor")
	public String submitDoctor(@ModelAttribute Doctor doc, Model model) {
		model.addAttribute("specialities", specRepo.findAll());
		model.addAttribute("doctors", doctorRepo.findAll());
		doctorRepo.save(doc);
		model.addAttribute("doctor", doc);
		return "add-doctor";
	}
	
	@GetMapping("/add-speciality")
	public String addSpeciality(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("speciality", new Speciality());
		return "add-speciality";
	}
	
	@PostMapping("/add-speciality")
	public String submitSpeciality(@ModelAttribute Speciality spec, Model model) {
		specRepo.save(spec);
		model.addAttribute("speciality", spec);
		return "add-speciality";
	}
}
