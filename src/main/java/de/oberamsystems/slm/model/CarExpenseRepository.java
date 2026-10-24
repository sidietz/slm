package de.oberamsystems.slm.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarExpenseRepository extends JpaRepository<CarExpense, UUID> {

	List<CarExpense> findAllByOrderByExpenseDateDesc();

	List<CarExpense> findByCarIdOrderByExpenseDateDesc(Long carId);

	List<CarExpense> findByCarIdOrderByExpenseDateAsc(Long carId);

	List<CarExpense> findByCarIdAndExpenseType(Long carId, CarExpenseType expenseType);

	void deleteByCarId(Long carId);
}
