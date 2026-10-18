package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "human_event")
public class HumanEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "human_id", nullable = false)
	private Human human;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "secondary_human_id")
	private Human secondaryHuman;

	@Column(nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@JdbcType(value = PostgreSQLEnumJdbcType.class)
	@Column(name = "event_type", nullable = false)
	private EventType eventType = EventType.CUSTOM;

	@Column(name = "event_date", nullable = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate eventDate;

	@Column(name = "annual_recurrence", nullable = false)
	private boolean annualRecurrence = true;

	private String notes;

	@Formula(value = "days_until_annual_occurrence(event_date)")
	private Integer daysUntilNext;

	@Formula(value = "next_annual_occurrence(event_date)")
	private LocalDate nextOccurrence;

	@Formula(value = "EXTRACT(YEAR FROM next_annual_occurrence(event_date)) - EXTRACT(YEAR FROM event_date)")
	private Integer yearsElapsed;

	public HumanEvent() {
	}

	public HumanEvent(Human human, String title, EventType eventType, LocalDate eventDate) {
		this.human = human;
		this.title = title;
		this.eventType = eventType;
		this.eventDate = eventDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Human getHuman() {
		return human;
	}

	public void setHuman(Human human) {
		this.human = human;
	}

	public Human getSecondaryHuman() {
		return secondaryHuman;
	}

	public void setSecondaryHuman(Human secondaryHuman) {
		this.secondaryHuman = secondaryHuman;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}

	public boolean isAnnualRecurrence() {
		return annualRecurrence;
	}

	public void setAnnualRecurrence(boolean annualRecurrence) {
		this.annualRecurrence = annualRecurrence;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Integer getDaysUntilNext() {
		return daysUntilNext;
	}

	public LocalDate getNextOccurrence() {
		return nextOccurrence;
	}

	public Integer getYearsElapsed() {
		return yearsElapsed;
	}
}
