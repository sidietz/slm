package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String firstname;
	private String lastname;
	@ManyToOne
	@JoinColumn(name = "speciality", referencedColumnName = "id")
	private Speciality speciality;
	private String place;
	private String city;
	private String plz;
	private float airDistance;
	
	public Doctor() {
	}
	
	public Doctor(Long id, String firstname, String lastname, Speciality speciality, String place, String city,
			String plz, float airDistance) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.speciality = speciality;
		this.place = place;
		this.city = city;
		this.plz = plz;
		this.airDistance = airDistance;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Speciality getSpeciality() {
		return speciality;
	}

	public void setSpeciality(Speciality speciality) {
		this.speciality = speciality;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPlz() {
		return plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public float getAirDistance() {
		return airDistance;
	}

	public void setAirDistance(float airDistance) {
		this.airDistance = airDistance;
	}

	public Long getId() {
		return id;
	}
}
