package de.oberamsystems.slm.controller;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import de.oberamsystems.slm.config.TimeZoneConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.oberamsystems.slm.model.Appointment;
import de.oberamsystems.slm.model.AppointmentRepository;
import de.oberamsystems.slm.model.Doctor;
import de.oberamsystems.slm.model.DoctorRepository;
import de.oberamsystems.slm.model.Point;
import de.oberamsystems.slm.model.PointRepository;
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
	
	@Autowired
	private PointRepository pointRepo;
	
	@GetMapping("/appointments")
	public String addAppointments(@RequestParam(required = false) Long id, Model model) {
		Appointment a = new Appointment();
		a.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setEnd(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setDeparture(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setArrival(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setArrivalHome(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("appointment", a);
		model.addAttribute("doctors", doctorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		model.addAttribute("appointments", repo.findAll(Sort.by(Sort.Direction.DESC, "departure")));
		return "appointments";
	}
	
	public String submitAppointment(Appointment app, Model model) {
		return submitAppointment(app, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/appointments")
	public String submitAppointment(@ModelAttribute Appointment app, Model model, RedirectAttributes redirectAttributes) {
		if (app.getDoctor() == null || app.getDoctor().getId() == null || !doctorRepo.existsById(app.getDoctor().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Doctor is required for appointment.");
			return "redirect:/appointments";
		}
		if (app.getId() != null && (app.getId() <= 0 || !repo.existsById(app.getId()))) {
			app.setId(null);
		}
		repo.save(app);
		return "redirect:/appointments";
	}
	
	@GetMapping("/doctors")
	public String addDoctors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("specialities", specRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("doctors", doctorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		model.addAttribute("points", pointRepo.findAll(Sort.by(Sort.Direction.DESC, "isHome").and(Sort.by(Sort.Direction.ASC, "name"))));
		Doctor d = new Doctor();
		pointRepo.findFirstByIsHomeTrue().ifPresent(d::setBasePoint);
		model.addAttribute("doctor", d);
		return "doctors";
	}
	
	public String submitDoctors(Doctor doc, Model model) {
		return submitDoctors(doc, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/doctors")
	public String submitDoctors(@ModelAttribute Doctor doc, Model model, RedirectAttributes redirectAttributes) {
		if (doc.getSpeciality() == null || doc.getSpeciality().getId() == null || !specRepo.existsById(doc.getSpeciality().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Doctor speciality is required.");
			return "redirect:/doctors";
		}
		if (doc.getBasePoint() == null || doc.getBasePoint().getId() == null) {
			pointRepo.findFirstByIsHomeTrue().ifPresent(doc::setBasePoint);
		}
		if (doc.getPoint() != null && doc.getPoint().getId() != null && doc.getBasePoint() != null && doc.getBasePoint().getId() != null) {
			try {
				Double dist = doctorRepo.calculateAirDistance(doc.getBasePoint().getId(), doc.getPoint().getId());
				if (dist != null) {
					doc.setAirDistance(dist);
				}
			} catch (Exception e) {
				log.warn("Could not calculate doctor air distance via PostGIS: {}", e.getMessage());
			}
		}
		if (doc.getId() != null && (doc.getId() <= 0 || !doctorRepo.existsById(doc.getId()))) {
			doc.setId(null);
		}
		doctorRepo.save(doc);
		return "redirect:/doctors";
	}

	@GetMapping("/specialities")
	public String addSpecialities(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("speciality", new Speciality());
		model.addAttribute("specialities", specRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		return "specialities";
	}

	@PostMapping("/specialities")
	public String submitSpecialities(@ModelAttribute Speciality spec, Model model) {
		if (spec.getId() != null && (spec.getId() <= 0 || !specRepo.existsById(spec.getId()))) {
			spec.setId(null);
		}
		specRepo.save(spec);
		model.addAttribute("speciality", spec);
		model.addAttribute("specialities", specRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		return "redirect:/specialities";
	}

	@GetMapping("/appointment")
	public String getSport(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? OffsetDateTime.now(TimeZoneConfig.getZoneId()).minusDays(90) : fromDate;
		toDate = toDate == null ? OffsetDateTime.now(TimeZoneConfig.getZoneId()) : toDate;

		OffsetDateTime minDate = repo.findMinDate();
	    OffsetDateTime maxDate = repo.findMaxDate();

	    minDate = minDate == null ? OffsetDateTime.now(TimeZoneConfig.getZoneId()).minusDays(90) : minDate.minusDays(1); //fixes off by one bug
	    maxDate = maxDate == null ? OffsetDateTime.now(TimeZoneConfig.getZoneId()): maxDate;

		model.addAttribute("appointments", repo.findByStartBetween(fromDate, toDate.plusDays(1), Sort.by(Sort.Direction.DESC, "start"))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "appointment";
	}
	
	@GetMapping({"/doctor.html", "/doctor"})
	public String index(Model model) {
		List<Doctor> docs = doctorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname")));
		model.addAttribute("doctors", docs);
		return "doctor";
	}
	
	@GetMapping({"/speciality.html", "/speciality"})
	public String speciality(Model model) {
		List<Speciality> specs = specRepo.findAll(Sort.by(Sort.Direction.ASC, "title"));
		model.addAttribute("specialities", specs);
		return "speciality";
	}
	
	@GetMapping("/add-appointment")
	public String addSport(@RequestParam(required = false) Long id, Model model) {
		Appointment a = new Appointment();
		a.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setEnd(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setDeparture(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setArrival(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		a.setArrivalHome(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("appointment", a);
		model.addAttribute("doctors", doctorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		return "add-appointment";
	}
	
	@PostMapping("/add-appointment")
	public String submitSport(@ModelAttribute Appointment app, Model model) {
		model.addAttribute("appointments", doctorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		repo.save(app);
		model.addAttribute("appointment", app);
		return "add-appointment";
	}
	
	@GetMapping("/add-doctor")
	public String addDoctor(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("specialities", specRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("points", pointRepo.findAll(Sort.by(Sort.Direction.DESC, "isHome").and(Sort.by(Sort.Direction.ASC, "name"))));
		Doctor d = new Doctor();
		pointRepo.findFirstByIsHomeTrue().ifPresent(d::setBasePoint);
		model.addAttribute("doctor", d);
		return "add-doctor";
	}
	
	@PostMapping("/add-doctor")
	public String submitDoctor(@ModelAttribute Doctor doc, Model model) {
		model.addAttribute("specialities", specRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("doctors", doctorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))));
		model.addAttribute("points", pointRepo.findAll(Sort.by(Sort.Direction.DESC, "isHome").and(Sort.by(Sort.Direction.ASC, "name"))));
		if (doc.getBasePoint() == null || doc.getBasePoint().getId() == null) {
			pointRepo.findFirstByIsHomeTrue().ifPresent(doc::setBasePoint);
		}
		if (doc.getPoint() != null && doc.getPoint().getId() != null && doc.getBasePoint() != null && doc.getBasePoint().getId() != null) {
			try {
				Double dist = doctorRepo.calculateAirDistance(doc.getBasePoint().getId(), doc.getPoint().getId());
				if (dist != null) {
					doc.setAirDistance(dist);
				}
			} catch (Exception e) {
				log.warn("Could not calculate doctor air distance via PostGIS: {}", e.getMessage());
			}
		}
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
