package de.oberamsystems.slm.model;

import java.time.LocalDateTime;

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
	@Column(name = "last_done", columnDefinition = "TIMESTAMP")
	private LocalDateTime lastDone;

	public HabitEntry() {
	}	

	public HabitEntry(Long id, Habit habit, LocalDateTime lastDone) {
		this.id = id;
		this.habit = habit;
		this.lastDone = lastDone;
	}

	public Long getId() {
		return id;
	}

	public Habit getHabit() {
		return habit;
	}

	public void setHabit(Habit habit) {
		this.habit = habit;
	}

	public LocalDateTime getLastDone() {
		return lastDone;
	}

	public void setLastDone(LocalDateTime lastDone) {
		this.lastDone = lastDone;
	}
}
