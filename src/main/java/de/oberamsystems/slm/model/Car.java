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
	private double purchasePrice;
	private double sellPrice;
	private long purchaseKilometers;
	private long sellKilometers;
	@Column(name = "price_per_100_km")
	private double pricePer100km;

	public Car() {
	}

	public Car(Long id, String vin, String name, String modelName, String keyNumber, Manufacturer manufacturer,
			Vendor vendor, LocalDate purchaseDate, LocalDate approvalDate, LocalDate manufacturingDate,
			double purchasePrice, double sellPrice, long purchaseKilometers, long sellKilometers,
			double pricePer100km) {
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

	public double getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public double getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public long getPurchaseKilometers() {
		return purchaseKilometers;
	}

	public void setPurchaseKilometers(long purchaseKilometers) {
		this.purchaseKilometers = purchaseKilometers;
	}

	public long getSellKilometers() {
		return sellKilometers;
	}

	public void setSellKilometers(long sellKilometers) {
		this.sellKilometers = sellKilometers;
	}

	public double getPricePer100km() {
		return pricePer100km;
	}

	public void setPricePer100km(double pricePer100km) {
		this.pricePer100km = pricePer100km;
	}

	public Long getId() {
		return id;
	}
}
