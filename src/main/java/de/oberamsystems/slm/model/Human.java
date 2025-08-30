package de.oberamsystems.slm.model;

import java.util.Date;

import org.hibernate.annotations.Formula;
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
	
	@Formula(value = "date_part('year', age(birthday))")
	private int age;
	
	
	/*
	chatgpt solution
	@Formula(
        "EXTRACT(DAY FROM (" +
        "CASE " +
        " WHEN (birthdate + (date_part('year', age(current_date, birthdate)) + 1) * interval '1 year')::date <= current_date " +
        " THEN (birthdate + (date_part('year', age(current_date, birthdate)) + 2) * interval '1 year')::date - current_date " +
        " ELSE (birthdate + (date_part('year', age(current_date, birthdate)) + 1) * interval '1 year')::date - current_date " +
        "END))"
    )
	 */
	@Formula(value = "CASE WHEN extract(day FROM this_years_birthday(birthday) - now()) <= 0 THEN extract(day FROM this_years_birthday(birthday) - now()) + 365 ELSE extract(day FROM this_years_birthday(birthday) - now()) END")
	private int daysUntilBirthday;
	
	public Human() {
	}

	public Human(long id, String firstname, String lastname, Date birthday) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.birthday = birthday;
	}
	
	public long getId() {
		return id;
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

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public int getAge() {
		return age;
	}

	public int getDaysUntilBirthday() {
		return daysUntilBirthday;
	}
	
	
}
