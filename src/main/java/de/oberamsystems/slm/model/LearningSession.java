package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "learning_session")
@DiscriminatorValue("LEARNING")
public class LearningSession extends Session {

	@ManyToOne
	@JoinColumn(name = "learning_item_id", referencedColumnName = "id")
	private LearningItem learningItem;

	@Column(name = "units_completed")
	private Integer unitsCompleted;

	public LearningSession() {
		super();
	}

	public LearningSession(LearningItem learningItem, Integer unitsCompleted) {
		super();
		this.learningItem = learningItem;
		this.unitsCompleted = unitsCompleted;
	}

	public LearningItem getLearningItem() {
		return learningItem;
	}

	public void setLearningItem(LearningItem learningItem) {
		this.learningItem = learningItem;
	}

	public Integer getUnitsCompleted() {
		return unitsCompleted;
	}

	public void setUnitsCompleted(Integer unitsCompleted) {
		this.unitsCompleted = unitsCompleted;
	}

	@Override
	public String toString() {
		return String.format("LearningSession [id=%s, learningItem=%s, unitsCompleted=%s, session=%s]",
				getId(), learningItem, unitsCompleted, super.toString());
	}
}
