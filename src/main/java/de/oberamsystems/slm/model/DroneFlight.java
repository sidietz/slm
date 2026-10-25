package de.oberamsystems.slm.model;

import io.hypersistence.utils.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;
import org.hibernate.generator.EventType;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "drone_flight")
public class DroneFlight {

    @Id
    @GeneratedValue
    @UuidGenerator(style = Style.VERSION_7)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @Column(name = "starttime", columnDefinition = "TIMESTAMPTZ", nullable = false)
    private OffsetDateTime start;

    @Column(name = "endtime", columnDefinition = "TIMESTAMPTZ", nullable = false)
    private OffsetDateTime end;

    @Type(PostgreSQLIntervalType.class)
    @Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    private Duration duration;

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "landing_latitude")
    private Double landingLatitude;

    @Column(name = "landing_longitude")
    private Double landingLongitude;

    @Column(name = "air_distance", insertable = false, updatable = false)
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    private Double airDistance;

    @Column(name = "max_altitude_meters")
    private Double maxAltitudeMeters;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public DroneFlight() {
    }

    public DroneFlight(Drone drone, OffsetDateTime start, OffsetDateTime end) {
        this.drone = drone;
        this.start = start;
        this.end = end;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Drone getDrone() {
        return drone;
    }

    public void setDrone(Drone drone) {
        this.drone = drone;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getLandingLatitude() {
        return landingLatitude;
    }

    public void setLandingLatitude(Double landingLatitude) {
        this.landingLatitude = landingLatitude;
    }

    public Double getLandingLongitude() {
        return landingLongitude;
    }

    public void setLandingLongitude(Double landingLongitude) {
        this.landingLongitude = landingLongitude;
    }

    public Double getAirDistance() {
        return airDistance;
    }

    public void setAirDistance(Double airDistance) {
        this.airDistance = airDistance;
    }

    public Double getMaxAltitudeMeters() {
        return maxAltitudeMeters;
    }

    public void setMaxAltitudeMeters(Double maxAltitudeMeters) {
        this.maxAltitudeMeters = maxAltitudeMeters;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean hasCoordinates() {
        return startLatitude != null && startLongitude != null;
    }

    public boolean isRoundTrip() {
        if (startLatitude == null || landingLatitude == null) return false;
        return Math.abs(startLatitude - landingLatitude) < 0.0001 &&
               Math.abs(startLongitude - landingLongitude) < 0.0001;
    }

    public String getAirDistanceFormatted() {
        if (airDistance == null || airDistance <= 0.0) return "-";
        if (airDistance >= 1000.0) {
            return String.format(java.util.Locale.US, "%.2f km", airDistance / 1000.0);
        } else {
            return String.format(java.util.Locale.US, "%.0f m", airDistance);
        }
    }

    public String getFormattedCoordinates() {
        if (startLatitude == null || startLongitude == null) return "None";
        return String.format(java.util.Locale.US, "%.4f, %.4f", startLatitude, startLongitude);
    }

    public String getFormattedLandingCoordinates() {
        if (landingLatitude == null || landingLongitude == null) return "-";
        return String.format(java.util.Locale.US, "%.4f, %.4f", landingLatitude, landingLongitude);
    }
}
