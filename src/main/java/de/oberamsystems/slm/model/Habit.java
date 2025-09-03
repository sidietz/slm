package de.oberamsystems.slm.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
public class Habit {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	@OneToMany(mappedBy="habit")
	private List<HabitEntry> entries;
	
	public Habit() {
	}

	public Habit(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Habit(Long id, String name, String description, List<HabitEntry> entries) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.entries = entries;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<HabitEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<HabitEntry> entries) {
		this.entries = entries;
	}

}
