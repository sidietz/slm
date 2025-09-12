package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Device {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String modelName;
	private String serialNumber;
	@Column(name = "purchase_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate purchaseDate;
	private float price;
	@ManyToOne
	@JoinColumn(name="type", referencedColumnName = "id")
	private DeviceType deviceType;
	@ManyToOne
	@JoinColumn(name="vendor_id", referencedColumnName = "id")
	private Vendor vendor;
	@ManyToOne
	@JoinColumn(name="manufacturer_id", referencedColumnName = "id")
	private Manufacturer manufacturer;

	public Device() {
	}

	public Device(Long id, String name, String modelName, String serialNumber, DeviceType deviceType,
			LocalDate purchaseDate, float price, Vendor vendor, Manufacturer manufacturer) {
		super();
		this.id = id;
		this.name = name;
		this.modelName = modelName;
		this.serialNumber = serialNumber;
		this.deviceType = deviceType;
		this.purchaseDate = purchaseDate;
		this.price = price;
		this.vendor = vendor;
		this.manufacturer = manufacturer;
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

	public void setModelName(String model_name) {
		this.modelName = model_name;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public LocalDate getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(LocalDate purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Manufacturer getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(Manufacturer manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format(
				"Device [id=%s, name=%s, model_name=%s, serial_number=%s, deviceType=%s, purchaseDate=%s, price=%s, vendor=%s, manufacturer=%s]",
				id, name, modelName, serialNumber, deviceType, purchaseDate, price, vendor, manufacturer);
	}
}
