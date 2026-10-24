package de.oberamsystems.slm.model;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "habit_entry")
public class HabitEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name="habit", referencedColumnName = "id")
	private Habit habit;
	@Column(name = "last_done", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime lastDone;

	public HabitEntry() {
	}	

	public HabitEntry(Long id, Habit habit, OffsetDateTime lastDone) {
		this.id = id;
		this.habit = habit;
		this.lastDone = lastDone;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Habit getHabit() {
		return habit;
	}

	public void setHabit(Habit habit) {
		this.habit = habit;
	}

	public OffsetDateTime getLastDone() {
		return lastDone;
	}

	public void setLastDone(OffsetDateTime lastDone) {
		this.lastDone = lastDone;
	}
}
