package de.oberamsystems.slm.mcp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.oberamsystems.slm.model.Appointment;
import de.oberamsystems.slm.model.AppointmentRepository;
import de.oberamsystems.slm.model.Doctor;
import de.oberamsystems.slm.model.DoctorRepository;

@Service
public class McpMedicalTools {

	@Autowired
	private AppointmentRepository appointmentRepo;

	@Autowired
	private DoctorRepository doctorRepo;

	@Tool(description = "Retrieve upcoming medical and doctor appointments with doctor details, speciality, and practice distance")
	public List<Map<String, Object>> getUpcomingAppointments() {
		List<Appointment> all = appointmentRepo.findAll();
		LocalDateTime now = LocalDateTime.now();

		List<Appointment> upcoming = all.stream()
				.filter(a -> a.getStart() != null && !a.getStart().isBefore(now.minusHours(2)))
				.sorted(Comparator.comparing(Appointment::getStart))
				.toList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Appointment a : upcoming) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", a.getId());
			m.put("start", a.getStart() != null ? a.getStart().toString() : null);
			m.put("end", a.getEnd() != null ? a.getEnd().toString() : null);
			m.put("comment", a.getComment());

			Doctor doc = a.getDoctor();
			if (doc != null) {
				m.put("doctorName", doc.getFirstname() + " " + doc.getLastname());
				m.put("speciality", doc.getSpeciality() != null ? doc.getSpeciality().getTitle() : null);
				m.put("city", doc.getCity());
				m.put("airDistanceKm", doc.getAirDistance() != null ? String.format("%.2f", doc.getAirDistance() / 1000.0) : null);
			}
			result.add(m);
		}

		return result;
	}

	@Tool(description = "Search doctors by speciality keyword or doctor name, with practice location and distance from home")
	public List<Map<String, Object>> searchDoctors(
			@ToolParam(description = "Keyword to search (speciality, doctor name, or city)", required = false) String query) {

		List<Doctor> all = doctorRepo.findAll();
		String q = (query != null) ? query.trim().toLowerCase() : "";

		List<Doctor> matched = all.stream()
				.filter(d -> {
					if (q.isEmpty()) return true;
					String fn = d.getFirstname() != null ? d.getFirstname().toLowerCase() : "";
					String ln = d.getLastname() != null ? d.getLastname().toLowerCase() : "";
					String city = d.getCity() != null ? d.getCity().toLowerCase() : "";
					String spec = (d.getSpeciality() != null && d.getSpeciality().getTitle() != null)
							? d.getSpeciality().getTitle().toLowerCase() : "";
					return fn.contains(q) || ln.contains(q) || city.contains(q) || spec.contains(q);
				})
				.sorted(Comparator.comparing(Doctor::getLastname, Comparator.nullsLast(Comparator.naturalOrder())))
				.toList();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Doctor d : matched) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", d.getId());
			m.put("name", d.getFirstname() + " " + d.getLastname());
			m.put("speciality", d.getSpeciality() != null ? d.getSpeciality().getTitle() : null);
			m.put("city", d.getCity());
			m.put("place", d.getPlace());
			m.put("airDistanceKm", d.getAirDistance() != null ? String.format("%.2f", d.getAirDistance() / 1000.0) : null);
			result.add(m);
		}

		return result;
	}

	@Tool(description = "Schedule a new medical or doctor appointment")
	public Map<String, Object> scheduleAppointment(
			@ToolParam(description = "ID of the doctor") Long doctorId,
			@ToolParam(description = "Start datetime in format YYYY-MM-DDTHH:mm (e.g. 2026-11-05T10:00)") String startStr,
			@ToolParam(description = "End datetime in format YYYY-MM-DDTHH:mm (e.g. 2026-11-05T11:00)") String endStr,
			@ToolParam(description = "Appointment reason or comment (e.g. Annual checkup)", required = false) String comment) {

		Doctor doctor = doctorRepo.findById(doctorId)
				.orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

		Appointment app = new Appointment();
		app.setDoctor(doctor);
		app.setStart(LocalDateTime.parse(startStr));
		app.setEnd(LocalDateTime.parse(endStr));
		app.setComment(comment);
		app = appointmentRepo.save(app);

		Map<String, Object> res = new HashMap<>();
		res.put("id", app.getId());
		res.put("doctorName", doctor.getFirstname() + " " + doctor.getLastname());
		res.put("start", app.getStart().toString());
		res.put("status", "SUCCESS");
		return res;
	}
}
