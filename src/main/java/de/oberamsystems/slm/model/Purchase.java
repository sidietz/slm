package de.oberamsystems.slm.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
public class Purchase {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long id;
	private String title;
	@ManyToOne
	@JoinColumn(name="vendor", referencedColumnName = "id")
	private Vendor vendor;
	private float price;
	@Column(name = "purchase_date", columnDefinition = "DATE")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate purchaseDate;

	
	public Purchase() {
	}
	
	public Purchase(long id, String title, Vendor vendor, float price, LocalDate purchaseDate) {
		super();
		this.id = id;
		this.title = title;
		this.vendor = vendor;
		this.price = price;
		this.purchaseDate = purchaseDate;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public LocalDate getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(LocalDate purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
}
