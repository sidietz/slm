package de.oberamsystems.slm.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "working_session")
@DiscriminatorValue("WORKING")
public class WorkingSession extends Session {

	@ManyToOne
	@JoinColumn(name = "employer_id", referencedColumnName = "id")
	private Employer employer;

	@Column(name = "project", columnDefinition = "TEXT")
	private String project;

	@Column(name = "billable")
	private Boolean billable = true;

	@Column(name = "hourly_rate")
	private BigDecimal hourlyRate;

	@Column(name = "task_category", columnDefinition = "TEXT")
	private String taskCategory;

	public WorkingSession() {
		super();
	}

	public WorkingSession(Employer employer, String project, Boolean billable, BigDecimal hourlyRate,
			String taskCategory) {
		super();
		this.employer = employer;
		this.project = project;
		this.billable = billable;
		this.hourlyRate = hourlyRate;
		this.taskCategory = taskCategory;
	}

	public Employer getEmployer() {
		return employer;
	}

	public void setEmployer(Employer employer) {
		this.employer = employer;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public Boolean getBillable() {
		return billable;
	}

	public void setBillable(Boolean billable) {
		this.billable = billable;
	}

	public BigDecimal getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(BigDecimal hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public String getTaskCategory() {
		return taskCategory;
	}

	public void setTaskCategory(String taskCategory) {
		this.taskCategory = taskCategory;
	}

	@Override
	public String toString() {
		return String.format(
				"WorkingSession [id=%s, employer=%s, project=%s, billable=%s, hourlyRate=%s, taskCategory=%s, session=%s]",
				getId(), employer, project, billable, hourlyRate, taskCategory, super.toString());
	}
}
