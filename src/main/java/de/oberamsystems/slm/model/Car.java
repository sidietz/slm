package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Car {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String vin;
	private String name;
	private String modelName;
	private String keyNumber;
	@ManyToOne
	@JoinColumn(name="manufacturer_id", referencedColumnName = "id")
	private Manufacturer manufacturer;
	@ManyToOne
	@JoinColumn(name="vendor_id", referencedColumnName = "id")
	private Vendor vendor;
	@Column(name = "purchase_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate purchaseDate;
	@Column(name = "approval_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate approvalDate;
	@Column(name = "manufacturing_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate manufacturingDate;
	private Double purchasePrice;
	private Double sellPrice;
	private Long purchaseKilometers;
	private Long sellKilometers;
	@Column(name = "price_per_100_km")
	private Double pricePer100km;
	@Column(name = "target_sell_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate targetSellDate;
	@Column(name = "residual_value")
	private Double residualValue;

	public Car() {
	}

	public Car(Long id, String vin, String name, String modelName, String keyNumber, Manufacturer manufacturer,
			Vendor vendor, LocalDate purchaseDate, LocalDate approvalDate, LocalDate manufacturingDate,
			Double purchasePrice, Double sellPrice, Long purchaseKilometers, Long sellKilometers,
			Double pricePer100km) {
		super();
		this.id = id;
		this.vin = vin;
		this.name = name;
		this.modelName = modelName;
		this.keyNumber = keyNumber;
		this.manufacturer = manufacturer;
		this.vendor = vendor;
		this.purchaseDate = purchaseDate;
		this.approvalDate = approvalDate;
		this.manufacturingDate = manufacturingDate;
		this.purchasePrice = purchasePrice;
		this.sellPrice = sellPrice;
		this.purchaseKilometers = purchaseKilometers;
		this.sellKilometers = sellKilometers;
		this.pricePer100km = pricePer100km;
	}

	public Car(Long id, String vin, String name, String modelName, String keyNumber, Manufacturer manufacturer,
			Vendor vendor, LocalDate purchaseDate, LocalDate approvalDate, LocalDate manufacturingDate,
			double purchasePrice, double sellPrice, long purchaseKilometers, long sellKilometers,
			double pricePer100km) {
		this(id, vin, name, modelName, keyNumber, manufacturer, vendor, purchaseDate, approvalDate, manufacturingDate,
				Double.valueOf(purchasePrice), Double.valueOf(sellPrice), Long.valueOf(purchaseKilometers),
				Long.valueOf(sellKilometers), Double.valueOf(pricePer100km));
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getKeyNumber() {
		return keyNumber;
	}

	public void setKeyNumber(String keyNumber) {
		this.keyNumber = keyNumber;
	}

	public Manufacturer getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(Manufacturer manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public LocalDate getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(LocalDate purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public LocalDate getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(LocalDate approvalDate) {
		this.approvalDate = approvalDate;
	}

	public LocalDate getManufacturingDate() {
		return manufacturingDate;
	}

	public void setManufacturingDate(LocalDate manufacturingDate) {
		this.manufacturingDate = manufacturingDate;
	}

	public Double getPurchasePrice() {
		return purchasePrice != null ? purchasePrice : 0.0;
	}

	public void setPurchasePrice(Double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public void setPurchasePrice(double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public Double getSellPrice() {
		return sellPrice != null ? sellPrice : 0.0;
	}

	public void setSellPrice(Double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public Long getPurchaseKilometers() {
		return purchaseKilometers != null ? purchaseKilometers : 0L;
	}

	public void setPurchaseKilometers(Long purchaseKilometers) {
		this.purchaseKilometers = purchaseKilometers;
	}

	public void setPurchaseKilometers(long purchaseKilometers) {
		this.purchaseKilometers = purchaseKilometers;
	}

	public Long getSellKilometers() {
		return sellKilometers != null ? sellKilometers : 0L;
	}

	public void setSellKilometers(Long sellKilometers) {
		this.sellKilometers = sellKilometers;
	}

	public void setSellKilometers(long sellKilometers) {
		this.sellKilometers = sellKilometers;
	}

	public Double getPricePer100km() {
		return pricePer100km != null ? pricePer100km : 0.0;
	}

	public void setPricePer100km(Double pricePer100km) {
		this.pricePer100km = pricePer100km;
	}

	public void setPricePer100km(double pricePer100km) {
		this.pricePer100km = pricePer100km;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getTargetSellDate() {
		return targetSellDate;
	}

	public void setTargetSellDate(LocalDate targetSellDate) {
		this.targetSellDate = targetSellDate;
	}

	public Double getResidualValue() {
		return residualValue;
	}

	public void setResidualValue(Double residualValue) {
		this.residualValue = residualValue;
	}

	public void setResidualValue(double residualValue) {
		this.residualValue = residualValue;
	}
}
