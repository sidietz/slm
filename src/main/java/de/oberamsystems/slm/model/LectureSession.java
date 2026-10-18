package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lecture_session")
@DiscriminatorValue("LECTURE")
public class LectureSession extends Session {

	@ManyToOne
	@JoinColumn(name = "course_id")
	private Course course;

	@Column(name = "lecture_number")
	private Integer lectureNumber;

	@Column(name = "topic", columnDefinition = "TEXT")
	private String topic;

	@Column(name = "room", columnDefinition = "TEXT")
	private String room;

	@Column(name = "recording_url", columnDefinition = "TEXT")
	private String recordingUrl;

	@Column(name = "slides_url", columnDefinition = "TEXT")
	private String slidesUrl;

	public LectureSession() {
		super();
	}

	public LectureSession(Course course, Integer lectureNumber, String topic, String room) {
		super();
		this.course = course;
		this.lectureNumber = lectureNumber;
		this.topic = topic;
		this.room = room;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Integer getLectureNumber() {
		return lectureNumber;
	}

	public void setLectureNumber(Integer lectureNumber) {
		this.lectureNumber = lectureNumber;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getRecordingUrl() {
		return recordingUrl;
	}

	public void setRecordingUrl(String recordingUrl) {
		this.recordingUrl = recordingUrl;
	}

	public String getSlidesUrl() {
		return slidesUrl;
	}

	public void setSlidesUrl(String slidesUrl) {
		this.slidesUrl = slidesUrl;
	}

	@Override
	public String toString() {
		return String.format("LectureSession [id=%s, course=%s, lectureNumber=%s, topic=%s, session=%s]",
				getId(), course, lectureNumber, topic, super.toString());
	}
}
