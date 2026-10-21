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
	@ManyToOne
	@JoinColumn(name = "point_id", referencedColumnName = "id")
	private Point point;
	@ManyToOne
	@JoinColumn(name = "base_point_id", referencedColumnName = "id")
	private Point basePoint;
	private Double airDistance;
	
	public Doctor() {
	}
	
	public Doctor(Long id, String firstname, String lastname, Speciality speciality, String place, String city,
			String plz, Double airDistance) {
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

	public Doctor(Long id, String firstname, String lastname, Speciality speciality, String place, String city,
			String plz, Point point, Point basePoint, Double airDistance) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.speciality = speciality;
		this.place = place;
		this.city = city;
		this.plz = plz;
		this.point = point;
		this.basePoint = basePoint;
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

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public Point getBasePoint() {
		return basePoint;
	}

	public void setBasePoint(Point basePoint) {
		this.basePoint = basePoint;
	}

	public Double getAirDistance() {
		return airDistance;
	}

	public void setAirDistance(Double airDistance) {
		this.airDistance = airDistance;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
