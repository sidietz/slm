package de.oberamsystems.slm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import de.oberamsystems.slm.config.TimeZoneConfig;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "car_expense")
public class CarExpense {

	@Id
	@GeneratedValue
	@UuidGenerator(style = Style.VERSION_7)
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_id", nullable = false)
	private Car car;

	@Enumerated(EnumType.STRING)
	@JdbcType(PostgreSQLEnumJdbcType.class)
	@Column(name = "expense_type", nullable = false)
	private CarExpenseType expenseType;

	@Column(name = "expense_date", nullable = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate expenseDate;

	@Column(name = "odometer", precision = 10, scale = 2)
	private BigDecimal odometer;

	@Column(name = "amount", precision = 10, scale = 2, nullable = false)
	private BigDecimal amount;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "note")
	private String note;

	@Column(name = "created_at", columnDefinition = "TIMESTAMPTZ")
	private OffsetDateTime createdAt;

	public CarExpense() {
	}

	public CarExpense(Car car, CarExpenseType expenseType, LocalDate expenseDate, BigDecimal odometer, BigDecimal amount, String title, String note) {
		this.car = car;
		this.expenseType = expenseType;
		this.expenseDate = expenseDate;
		this.odometer = odometer;
		this.amount = amount;
		this.title = title;
		this.note = note;
	}

	@PrePersist
	public void prePersist() {
		if (this.createdAt == null) {
			this.createdAt = OffsetDateTime.now(TimeZoneConfig.getZoneId());
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public CarExpenseType getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(CarExpenseType expenseType) {
		this.expenseType = expenseType;
	}

	public LocalDate getExpenseDate() {
		return expenseDate;
	}

	public void setExpenseDate(LocalDate expenseDate) {
		this.expenseDate = expenseDate;
	}

	public BigDecimal getOdometer() {
		return odometer;
	}

	public void setOdometer(BigDecimal odometer) {
		this.odometer = odometer;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isCredit() {
		return expenseType != null && expenseType.isCredit();
	}
}
