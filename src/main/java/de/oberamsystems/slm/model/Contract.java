package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.hibernate.annotations.Formula;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Contract {

	public enum ContractStatus {
		ACTIVE_INITIAL_TERM,
		ACTIVE_AUTO_EXTENDED,
		CANCELED_PENDING,
		TERMINATED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	@ManyToOne
	@JoinColumn(name = "contractor_id", referencedColumnName = "id")
	private Contractor contractor;
	private float fee;
	@Column(name = "start_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
	@Column(name = "initial_end_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate initialEndDate;
	
	@Column(name = "auto_extension", nullable = false)
	private boolean autoExtension = true;

	@Column(name = "auto_extension_months", nullable = false)
	private int autoExtensionMonths = 1;

	@Column(name = "is_canceled", nullable = false)
	private boolean isCanceled = false;

	@Column(name = "cancellation_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate cancellationDate;

	@Column(name = "termination_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate terminationDate;

	@Formula(value = "CASE " +
			"WHEN active = FALSE THEN 0 " +
			"WHEN is_canceled = TRUE AND termination_date IS NOT NULL THEN GREATEST(0, (termination_date - CURRENT_DATE)) " +
			"WHEN is_canceled = TRUE AND initial_end_date IS NOT NULL THEN GREATEST(0, (initial_end_date - CURRENT_DATE)) " +
			"WHEN initial_end_date IS NOT NULL AND initial_end_date >= CURRENT_DATE THEN (initial_end_date - CURRENT_DATE) " +
			"WHEN initial_end_date IS NOT NULL AND initial_end_date < CURRENT_DATE AND auto_extension = TRUE THEN NULL " +
			"WHEN initial_end_date IS NOT NULL AND initial_end_date < CURRENT_DATE AND auto_extension = FALSE THEN 0 " +
			"ELSE NULL END")
	private Integer daysRemaining;

	@Column(name = "active")
	private boolean isActive = true;
	
	public Contract() {
	}

	public Contract(Long id, String title, Contractor contractor, float fee, LocalDate startDate, LocalDate initialEndDate,
			Integer daysRemaining, boolean isActive) {
		super();
		this.id = id;
		this.title = title;
		this.contractor = contractor;
		this.fee = fee;
		this.startDate = startDate;
		this.initialEndDate = initialEndDate;
		this.daysRemaining = daysRemaining;
		this.isActive = isActive;
	}

	public ContractStatus getCalculatedStatus() {
		if (!isActive) {
			return ContractStatus.TERMINATED;
		}
		LocalDate today = LocalDate.now();
		if (isCanceled) {
			if (terminationDate != null && today.isAfter(terminationDate)) {
				return ContractStatus.TERMINATED;
			}
			return ContractStatus.CANCELED_PENDING;
		}
		if (initialEndDate != null) {
			if (today.isAfter(initialEndDate)) {
				return autoExtension ? ContractStatus.ACTIVE_AUTO_EXTENDED : ContractStatus.TERMINATED;
			}
			return ContractStatus.ACTIVE_INITIAL_TERM;
		}
		return ContractStatus.ACTIVE_AUTO_EXTENDED;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public LocalDate getInitialEndDate() {
		return initialEndDate;
	}

	public void setInitialEndDate(LocalDate initialEndDate) {
		this.initialEndDate = initialEndDate;
	}

	// Backward-compatibility aliases for getEndDate / setEndDate
	public LocalDate getEndDate() {
		return initialEndDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.initialEndDate = endDate;
	}

	public boolean isAutoExtension() {
		return autoExtension;
	}

	public void setAutoExtension(boolean autoExtension) {
		this.autoExtension = autoExtension;
	}

	public int getAutoExtensionMonths() {
		return autoExtensionMonths;
	}

	public void setAutoExtensionMonths(int autoExtensionMonths) {
		this.autoExtensionMonths = autoExtensionMonths;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public void setIsCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public boolean getIsCanceled() {
		return isCanceled;
	}

	public LocalDate getCancellationDate() {
		return cancellationDate;
	}

	public void setCancellationDate(LocalDate cancellationDate) {
		this.cancellationDate = cancellationDate;
	}

	public LocalDate getTerminationDate() {
		return terminationDate;
	}

	public void setTerminationDate(LocalDate terminationDate) {
		this.terminationDate = terminationDate;
	}

	public Integer getDaysRemaining() {
		return daysRemaining;
	}

	public void setDaysRemaining(Integer daysRemaining) {
		this.daysRemaining = daysRemaining;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public boolean getIsActive() {
		return isActive;
	}
}
