package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Controller
public class DroneController {

    private static final Logger log = LoggerFactory.getLogger(DroneController.class);

    private final DroneRepository droneRepository;
    private final DroneFlightRepository droneFlightRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final VendorRepository vendorRepository;

    public DroneController(DroneRepository droneRepository,
                           DroneFlightRepository droneFlightRepository,
                           ManufacturerRepository manufacturerRepository,
                           VendorRepository vendorRepository) {
        this.droneRepository = droneRepository;
        this.droneFlightRepository = droneFlightRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.vendorRepository = vendorRepository;
    }

    @GetMapping("/drones")
    public String getDrones(@RequestParam(required = false) DroneCategory category,
                            Model model) {
        List<Drone> drones;
        if (category != null) {
            drones = droneRepository.findByCategoryOrderByNameAsc(category);
        } else {
            drones = droneRepository.findAllByOrderByNameAsc();
        }

        long totalFlights = 0;
        Duration totalFlightDuration = Duration.ZERO;
        double totalAirDistanceMeters = 0.0;

        for (Drone d : drones) {
            totalFlights += d.getFlightCount();
            totalFlightDuration = totalFlightDuration.plus(d.getTotalFlightDuration());
            totalAirDistanceMeters += d.getTotalAirDistance();
        }

        model.addAttribute("drones", drones);
        model.addAttribute("categories", Arrays.asList(DroneCategory.values()));
        model.addAttribute("selectedCategory", category);
        model.addAttribute("manufacturers", manufacturerRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("vendors", vendorRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("newDrone", new Drone());

        model.addAttribute("totalFleetCount", drones.size());
        model.addAttribute("totalFlightsCount", totalFlights);
        model.addAttribute("totalFlightHours", formatDurationHours(totalFlightDuration));
        model.addAttribute("totalDistanceKm", Math.round((totalAirDistanceMeters / 1000.0) * 10.0) / 10.0);

        return "drones";
    }

    @PostMapping("/drones")
    public String saveDrone(@ModelAttribute Drone drone,
                            @RequestParam(value = "manufacturerId", required = false) Long manufacturerId,
                            @RequestParam(value = "vendorId", required = false) Long vendorId,
                            RedirectAttributes redirectAttributes) {
        try {
            if (manufacturerId != null) {
                drone.setManufacturer(manufacturerRepository.findById(manufacturerId).orElse(null));
            } else {
                drone.setManufacturer(null);
            }

            if (vendorId != null) {
                drone.setVendor(vendorRepository.findById(vendorId).orElse(null));
            } else {
                drone.setVendor(null);
            }

            if (drone.getId() != null) {
                Drone existing = droneRepository.findById(drone.getId()).orElse(null);
                if (existing != null) {
                    existing.setName(drone.getName());
                    existing.setModelName(drone.getModelName());
                    existing.setCategory(drone.getCategory());
                    existing.setWeightGrams(drone.getWeightGrams());
                    existing.setBuyDate(drone.getBuyDate());
                    existing.setSerialNumber(drone.getSerialNumber());
                    existing.setUasOperatorId(drone.getUasOperatorId());
                    existing.setPurchasePrice(drone.getPurchasePrice());
                    existing.setManufacturer(drone.getManufacturer());
                    existing.setVendor(drone.getVendor());
                    existing.setNotes(drone.getNotes());
                    existing.setActive(drone.getActive() != null ? drone.getActive() : true);
                    droneRepository.save(existing);
                    redirectAttributes.addFlashAttribute("successMessage", "Drone '" + existing.getName() + "' updated successfully.");
                    return "redirect:/drones";
                }
            }

            droneRepository.save(drone);
            redirectAttributes.addFlashAttribute("successMessage", "Drone '" + drone.getName() + "' registered successfully.");
        } catch (Exception e) {
            log.error("Failed to save drone", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving drone: " + e.getMessage());
        }
        return "redirect:/drones";
    }

    @GetMapping("/drones/{id}")
    public String getDroneDetail(@PathVariable UUID id, Model model) {
        Drone drone = droneRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid drone ID: " + id));
        List<DroneFlight> flights = droneFlightRepository.findByDroneIdOrderByStartDesc(id);

        Duration totalDuration = Duration.ZERO;
        double totalDistance = 0.0;
        for (DroneFlight f : flights) {
            if (f.getDuration() != null) {
                totalDuration = totalDuration.plus(f.getDuration());
            }
            if (f.getAirDistance() != null) {
                totalDistance += f.getAirDistance();
            }
        }

        model.addAttribute("drone", drone);
        model.addAttribute("flights", flights);
        model.addAttribute("categories", Arrays.asList(DroneCategory.values()));
        model.addAttribute("manufacturers", manufacturerRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("vendors", vendorRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("totalDurationHours", formatDurationHours(totalDuration));
        model.addAttribute("totalDistanceKm", Math.round((totalDistance / 1000.0) * 10.0) / 10.0);
        model.addAttribute("newFlight", new DroneFlight(drone, null, null));

        return "drones";
    }

    @PostMapping("/drones/{id}/delete")
    public String deleteDrone(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Drone drone = droneRepository.findById(id).orElse(null);
            if (drone != null) {
                String name = drone.getName();
                droneRepository.delete(drone);
                redirectAttributes.addFlashAttribute("successMessage", "Drone '" + name + "' and its flights were deleted.");
            }
        } catch (Exception e) {
            log.error("Failed to delete drone {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete drone: " + e.getMessage());
        }
        return "redirect:/drones";
    }

    @GetMapping("/drone-flights")
    public String getDroneFlights(@RequestParam(required = false) UUID droneId,
                                  Model model) {
        List<Drone> drones = droneRepository.findAllByOrderByNameAsc();
        List<DroneFlight> flights;

        if (droneId != null) {
            flights = droneFlightRepository.findByDroneIdOrderByStartDesc(droneId);
        } else {
            flights = droneFlightRepository.findAllByOrderByStartDesc();
        }

        Duration totalDuration = Duration.ZERO;
        double totalDistance = 0.0;
        for (DroneFlight f : flights) {
            if (f.getDuration() != null) {
                totalDuration = totalDuration.plus(f.getDuration());
            }
            if (f.getAirDistance() != null) {
                totalDistance += f.getAirDistance();
            }
        }

        model.addAttribute("flights", flights);
        model.addAttribute("drones", drones);
        model.addAttribute("selectedDroneId", droneId);
        model.addAttribute("newFlight", new DroneFlight());
        model.addAttribute("totalFlightCount", flights.size());
        model.addAttribute("totalDurationHours", formatDurationHours(totalDuration));
        model.addAttribute("totalDistanceKm", Math.round((totalDistance / 1000.0) * 10.0) / 10.0);

        return "drone-flights";
    }

    @PostMapping("/drone-flights")
    public String saveDroneFlight(@RequestParam(value = "id", required = false) UUID id,
                                  @RequestParam("droneId") UUID droneId,
                                  @RequestParam("start") OffsetDateTime start,
                                  @RequestParam("end") OffsetDateTime end,
                                  @RequestParam(value = "startLatitude", required = false) Double startLatitude,
                                  @RequestParam(value = "startLongitude", required = false) Double startLongitude,
                                  @RequestParam(value = "landingLatitude", required = false) Double landingLatitude,
                                  @RequestParam(value = "landingLongitude", required = false) Double landingLongitude,
                                  @RequestParam(value = "maxAltitudeMeters", required = false) Double maxAltitudeMeters,
                                  @RequestParam(value = "notes", required = false) String notes,
                                  @RequestParam(value = "redirectBack", required = false) String redirectBack,
                                  RedirectAttributes redirectAttributes) {
        try {
            Drone drone = droneRepository.findById(droneId)
                    .orElseThrow(() -> new IllegalArgumentException("Drone not found: " + droneId));

            if (end.isBefore(start)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Landing time cannot be before takeoff time.");
                return redirectBack != null && !redirectBack.isBlank() ? "redirect:" + redirectBack : "redirect:/drone-flights";
            }

            DroneFlight flight;
            if (id != null) {
                flight = droneFlightRepository.findById(id).orElse(new DroneFlight());
            } else {
                flight = new DroneFlight();
            }

            flight.setDrone(drone);
            flight.setStart(start);
            flight.setEnd(end);
            flight.setStartLatitude(startLatitude);
            flight.setStartLongitude(startLongitude);
            flight.setLandingLatitude(landingLatitude);
            flight.setLandingLongitude(landingLongitude);
            flight.setMaxAltitudeMeters(maxAltitudeMeters);
            flight.setNotes(notes);

            droneFlightRepository.save(flight);
            redirectAttributes.addFlashAttribute("successMessage", "Flight logged successfully for " + drone.getName() + ".");
        } catch (Exception e) {
            log.error("Failed to save drone flight", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error logging flight: " + e.getMessage());
        }

        return redirectBack != null && !redirectBack.isBlank() ? "redirect:" + redirectBack : "redirect:/drone-flights";
    }

    @PostMapping("/drone-flights/{id}/delete")
    public String deleteDroneFlight(@PathVariable UUID id,
                                    @RequestParam(value = "redirectBack", required = false) String redirectBack,
                                    RedirectAttributes redirectAttributes) {
        try {
            droneFlightRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Flight log deleted.");
        } catch (Exception e) {
            log.error("Failed to delete flight {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete flight: " + e.getMessage());
        }
        return redirectBack != null && !redirectBack.isBlank() ? "redirect:" + redirectBack : "redirect:/drone-flights";
    }

    private String formatDurationHours(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0.0 h";
        }
        long minutes = duration.toMinutes();
        double hours = minutes / 60.0;
        return String.format(Locale.US, "%.1f h", hours);
    }
}
