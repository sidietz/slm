package de.oberamsystems.slm.model;

import jakarta.persistence.*;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // Many addresses can belong to one human
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name = "human_id", nullable = false)
    private Human human;
    private String street;
    private Integer streetnumber;

    @Column(length = 5)
    private String plz;
    private String place;
    private String country;

    public Address() {
    	
    }
    
    public Address(long id, Human human, String street, Integer streetnumber, String plz, String place,
			String country) {
		this.id = id;
		this.human = human;
		this.street = street;
		this.streetnumber = streetnumber;
		this.plz = plz;
		this.place = place;
		this.country = country;
	}

	public Long getId() {
        return id;
    }

    public Human getHuman() {
        return human;
    }

    public void setHuman(Human human) {
        this.human = human;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getStreetnumber() {
        return streetnumber;
    }

    public void setStreetnumber(Integer streetnumber) {
        this.streetnumber = streetnumber;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
