package de.oberamsystems.slm.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TrainStation2Repository extends JpaRepository<TrainStation2, Long> {
	public List<TrainStation2> findByLinesIn(List<TrainLine> lines);
	
	public List<TrainStation2> findByLines_Id(@Param("id") long id);
	
}
