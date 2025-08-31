package de.oberamsystems.slm.controller;

import org.springframework.data.jpa.repository.JpaRepository;

import de.oberamsystems.slm.model.TrainStation;

public interface TrainStationRepository extends JpaRepository<TrainStation, Long> {

}
