package de.oberamsystems.slm.mcp;

import de.oberamsystems.slm.converter.StringToOffsetDateTimeConverter;
import de.oberamsystems.slm.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class McpDroneTools {

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DroneFlightRepository droneFlightRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Tool(description = "List all registered UAVs (drones) with category, weight, and flight metrics.")
    public List<Map<String, Object>> listDrones(
            @ToolParam(description = "Optional category filter: C0, C1, C2, C3, C4", required = false) String category) {
        List<Drone> drones;
        if (category != null && !category.isBlank()) {
            try {
                DroneCategory cat = DroneCategory.valueOf(category.trim().toUpperCase());
                drones = droneRepository.findByCategoryOrderByNameAsc(cat);
            } catch (IllegalArgumentException e) {
                drones = droneRepository.findAllByOrderByNameAsc();
            }
        } else {
            drones = droneRepository.findAllByOrderByNameAsc();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Drone d : drones) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId().toString());
            map.put("name", d.getName());
            map.put("modelName", d.getModelName());
            map.put("category", d.getCategory() != null ? d.getCategory().name() : null);
            map.put("weightGrams", d.getWeightGrams());
            map.put("buyDate", d.getBuyDate() != null ? d.getBuyDate().toString() : null);
            map.put("serialNumber", d.getSerialNumber());
            map.put("uasOperatorId", d.getUasOperatorId());
            map.put("manufacturer", d.getManufacturer() != null ? d.getManufacturer().getName() : null);
            map.put("vendor", d.getVendor() != null ? d.getVendor().getName() : null);
            map.put("flightCount", d.getFlightCount());
            result.add(map);
        }
        return result;
    }

    @Tool(description = "Register a new UAV (drone) in the system.")
    public Map<String, Object> registerDrone(
            @ToolParam(description = "Friendly name of the drone", required = true) String name,
            @ToolParam(description = "EU UAS Class: C0, C1, C2, C3, or C4", required = true) String category,
            @ToolParam(description = "Takeoff weight / MTOM in grams (e.g. 249)", required = true) int weightGrams,
            @ToolParam(description = "Model name (e.g. Mini 4 Pro)", required = false) String modelName,
            @ToolParam(description = "Purchase date in yyyy-MM-dd format", required = false) String buyDate,
            @ToolParam(description = "Serial number", required = false) String serialNumber,
            @ToolParam(description = "UAS Operator registration number (e-ID)", required = false) String uasOperatorId,
            @ToolParam(description = "Purchase price", required = false) Double purchasePrice,
            @ToolParam(description = "Notes", required = false) String notes) {

        Drone drone = new Drone();
        drone.setName(name);
        drone.setCategory(DroneCategory.valueOf(category.trim().toUpperCase()));
        drone.setWeightGrams(weightGrams);
        drone.setModelName(modelName);
        if (buyDate != null && !buyDate.isBlank()) {
            drone.setBuyDate(LocalDate.parse(buyDate.trim()));
        }
        drone.setSerialNumber(serialNumber);
        drone.setUasOperatorId(uasOperatorId);
        if (purchasePrice != null) {
            drone.setPurchasePrice(BigDecimal.valueOf(purchasePrice));
        }
        drone.setNotes(notes);

        Drone saved = droneRepository.save(drone);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "SUCCESS");
        res.put("id", saved.getId().toString());
        res.put("name", saved.getName());
        res.put("category", saved.getCategory().name());
        return res;
    }

    @Tool(description = "Log a drone flight with takeoff/landing timestamps, coordinates, and notes.")
    public Map<String, Object> logDroneFlight(
            @ToolParam(description = "UUID of the drone", required = true) String droneId,
            @ToolParam(description = "Takeoff start timestamp (ISO-8601 or yyyy-MM-dd'T'HH:mm)", required = true) String startDateTime,
            @ToolParam(description = "Landing end timestamp (ISO-8601 or yyyy-MM-dd'T'HH:mm)", required = true) String endDateTime,
            @ToolParam(description = "Takeoff latitude", required = false) Double startLatitude,
            @ToolParam(description = "Takeoff longitude", required = false) Double startLongitude,
            @ToolParam(description = "Landing latitude", required = false) Double landingLatitude,
            @ToolParam(description = "Landing longitude", required = false) Double landingLongitude,
            @ToolParam(description = "Max altitude reached in meters", required = false) Double maxAltitudeMeters,
            @ToolParam(description = "Flight mission notes / weather", required = false) String notes) {

        Drone drone = droneRepository.findById(UUID.fromString(droneId))
                .orElseThrow(() -> new IllegalArgumentException("Drone not found: " + droneId));

        OffsetDateTime start = StringToOffsetDateTimeConverter.parse(startDateTime);
        OffsetDateTime end = StringToOffsetDateTimeConverter.parse(endDateTime);

        DroneFlight flight = new DroneFlight();
        flight.setDrone(drone);
        flight.setStart(start);
        flight.setEnd(end);
        flight.setStartLatitude(startLatitude);
        flight.setStartLongitude(startLongitude);
        flight.setLandingLatitude(landingLatitude);
        flight.setLandingLongitude(landingLongitude);
        flight.setMaxAltitudeMeters(maxAltitudeMeters);
        flight.setNotes(notes);

        DroneFlight saved = droneFlightRepository.save(flight);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "SUCCESS");
        res.put("flightId", saved.getId().toString());
        res.put("droneName", drone.getName());
        res.put("start", saved.getStart().toString());
        res.put("end", saved.getEnd().toString());
        return res;
    }

    @Tool(description = "List recent drone flight logs, optionally filtered by drone.")
    public List<Map<String, Object>> listDroneFlights(
            @ToolParam(description = "Optional drone UUID filter", required = false) String droneId,
            @ToolParam(description = "Maximum number of flights to return (default 20)", required = false) Integer limit) {
        List<DroneFlight> flights;
        if (droneId != null && !droneId.isBlank()) {
            flights = droneFlightRepository.findByDroneIdOrderByStartDesc(UUID.fromString(droneId));
        } else {
            flights = droneFlightRepository.findAllByOrderByStartDesc();
        }

        int max = (limit != null && limit > 0) ? Math.min(limit, flights.size()) : Math.min(20, flights.size());
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            DroneFlight f = flights.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("flightId", f.getId().toString());
            map.put("droneId", f.getDrone().getId().toString());
            map.put("droneName", f.getDrone().getName());
            map.put("category", f.getDrone().getCategory().name());
            map.put("start", f.getStart().toString());
            map.put("end", f.getEnd().toString());
            map.put("startLatitude", f.getStartLatitude());
            map.put("startLongitude", f.getStartLongitude());
            map.put("landingLatitude", f.getLandingLatitude());
            map.put("landingLongitude", f.getLandingLongitude());
            map.put("airDistanceMeters", f.getAirDistance());
            map.put("notes", f.getNotes());
            result.add(map);
        }
        return result;
    }
}
