package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.hibernate.annotations.Formula;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Contract {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String title;
	@ManyToOne
	@JoinColumn(name = "contractor_id", referencedColumnName = "id")
	private Contractor contractor;
	private float fee;
	@Column(name = "start_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
	@Column(name = "end_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;
	
	@Formula(value = "extract(day FROM end_date - now())")
	private int daysRemaining;
	@Column(name = "active")
	private boolean isActive;
	
	public Contract() {
	}

	public Contract(long id, String title, Contractor contractor, float fee, LocalDate startDate, LocalDate endDate,
			int daysRemaining, boolean isActive) {
		super();
		this.id = id;
		this.title = title;
		this.contractor = contractor;
		this.fee = fee;
		this.startDate = startDate;
		this.endDate = endDate;
		this.daysRemaining = daysRemaining;
		this.isActive = isActive;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Contractor getContractor() {
		return contractor;
	}

	public void setContractor(Contractor contractor) {
		this.contractor = contractor;
	}

	public float getFee() {
		return fee;
	}

	public void setFee(float fee) {
		this.fee = fee;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public int getDaysRemaining() {
		return daysRemaining;
	}

	public void setDaysRemaining(int daysRemaining) {
		this.daysRemaining = daysRemaining;
	}

	public long getId() {
		return id;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
