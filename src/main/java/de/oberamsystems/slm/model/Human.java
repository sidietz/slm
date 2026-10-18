package de.oberamsystems.slm.model;

import java.util.Date;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Human {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;
	private String firstname;
	private String lastname;
	@Column(name = "birthday", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;
	
	@Column(name = "death_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date deathDate;
	
	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	private HumanCategory category;
	
	@Formula(value = "date_part('year', age(birthday))")
	private int age;
	
	@Formula(value = "CASE WHEN death_date IS NOT NULL THEN TRUE ELSE FALSE END")
	private Boolean deceased;
	
	@Formula(value = "CASE WHEN death_date IS NOT NULL THEN date_part('year', age(death_date, birthday)) ELSE NULL END")
	private Integer ageAtDeath;
	
	@Formula(value = "CASE WHEN extract(day FROM this_years_birthday(birthday) - now()) <= 0 THEN extract(day FROM this_years_birthday(birthday) - now()) + 365 ELSE extract(day FROM this_years_birthday(birthday) - now()) END")
	private int daysUntilBirthday;
	
	@OneToMany(mappedBy = "human", cascade = CascadeType.ALL, orphanRemoval = true)
	private java.util.List<HumanEvent> events = new java.util.ArrayList<>();
	
	public Human() {
	}

	public Human(long id, String firstname, String lastname, Date birthday, HumanCategory category) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.birthday = birthday;
		this.category = category;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public String getFullName() {
		String fn = firstname != null ? firstname : "";
		String ln = lastname != null ? lastname : "";
		String full = (fn + " " + ln).trim();
		return full.isEmpty() ? "Unknown Contact" : full;
	}

	public String getName() {
		return getFullName();
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Date getDeathDate() {
		return deathDate;
	}

	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}

	public Boolean getDeceased() {
		return deceased != null ? deceased : (deathDate != null);
	}

	public boolean isDeceased() {
		return deceased != null ? deceased : (deathDate != null);
	}

	public Integer getAgeAtDeath() {
		return ageAtDeath;
	}

	public int getAge() {
		return age;
	}

	public int getDaysUntilBirthday() {
		return daysUntilBirthday;
	}

	public HumanCategory getCategory() {
		return category;
	}

	public void setCategory(HumanCategory category) {
		this.category = category;
	}

	public java.util.List<HumanEvent> getEvents() {
		return events;
	}

	public void setEvents(java.util.List<HumanEvent> events) {
		this.events = events;
	}
}
