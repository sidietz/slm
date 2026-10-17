package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coding_session")
@DiscriminatorValue("CODING")
public class CodingSession extends Session {

	@Column(name = "project", columnDefinition = "TEXT")
	private String project;

	@Column(name = "repository", columnDefinition = "TEXT")
	private String repository;

	@Column(name = "branch", columnDefinition = "TEXT")
	private String branch;

	@Column(name = "ticket", columnDefinition = "TEXT")
	private String ticket;

	@Column(name = "language", columnDefinition = "TEXT")
	private String language;

	public CodingSession() {
		super();
	}

	public CodingSession(String project, String repository, String branch, String ticket, String language) {
		super();
		this.project = project;
		this.repository = repository;
		this.branch = branch;
		this.ticket = ticket;
		this.language = language;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String toString() {
		return String.format(
				"CodingSession [id=%s, project=%s, repository=%s, branch=%s, ticket=%s, language=%s, session=%s]",
				getId(), project, repository, branch, ticket, language, super.toString());
	}
}
