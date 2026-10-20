package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class EmployerContractIT extends AbstractIntegrationTest {

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    public void testEmployerAndContractPersistence() {
        // Test Employer directory sorting
        Employer e2 = employerRepository.saveAndFlush(new Employer("Zeta Consulting"));
        Employer e1 = employerRepository.saveAndFlush(new Employer("Alpha Tech"));

        List<Employer> sortedEmployers = employerRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        assertTrue(sortedEmployers.size() >= 2);
        int alphaIdx = -1;
        int zetaIdx = -1;
        for (int i = 0; i < sortedEmployers.size(); i++) {
            if ("Alpha Tech".equals(sortedEmployers.get(i).getName())) alphaIdx = i;
            if ("Zeta Consulting".equals(sortedEmployers.get(i).getName())) zetaIdx = i;
        }
        assertTrue(alphaIdx < zetaIdx, "Employers must be sorted alphabetically ascending");

        // Test Contractor and Contract linkage
        Contractor contractor = new Contractor();
        contractor.setName("Stadtwerke Ansbach");
        contractor = contractorRepository.saveAndFlush(contractor);

        Contract contract = new Contract();
        contract.setTitle("Electricity 2026");
        contract.setContractor(contractor);
        contract.setFee(85.50f);
        contract.setActive(true);
        contract.setStartDate(LocalDate.of(2026, 1, 1));
        contract = contractRepository.saveAndFlush(contract);

        assertNotNull(contract.getId());
        assertEquals("Stadtwerke Ansbach", contract.getContractor().getName());
        assertEquals(85.50f, contract.getFee());

        // 1. Initial term in the future
        Contract futureContract = new Contract();
        futureContract.setTitle("Internet 1000");
        futureContract.setContractor(contractor);
        futureContract.setFee(59.99f);
        futureContract.setActive(true);
        futureContract.setStartDate(LocalDate.now().minusMonths(6));
        futureContract.setInitialEndDate(LocalDate.now().plusMonths(6));
        futureContract.setAutoExtension(true);
        futureContract = contractRepository.saveAndFlush(futureContract);
        entityManager.refresh(futureContract);
        assertEquals(Contract.ContractStatus.ACTIVE_INITIAL_TERM, futureContract.getCalculatedStatus());
        assertNotNull(futureContract.getDaysRemaining());
        assertTrue(futureContract.getDaysRemaining() > 0, "Initial term contract must have positive days remaining");

        // 2. Initial term in the past, uncanceled with auto-extension enabled (Must not be negative!)
        Contract autoExtended = new Contract();
        autoExtended.setTitle("Mobile Unlimited");
        autoExtended.setContractor(contractor);
        autoExtended.setFee(29.99f);
        autoExtended.setActive(true);
        autoExtended.setStartDate(LocalDate.now().minusYears(2));
        autoExtended.setInitialEndDate(LocalDate.now().minusMonths(6));
        autoExtended.setAutoExtension(true);
        autoExtended = contractRepository.saveAndFlush(autoExtended);
        entityManager.refresh(autoExtended);
        assertEquals(Contract.ContractStatus.ACTIVE_AUTO_EXTENDED, autoExtended.getCalculatedStatus());
        assertNull(autoExtended.getDaysRemaining(), "Auto-extended contract must have null daysRemaining (never negative)");

        // 3. Canceled contract pending termination date
        Contract canceledContract = new Contract();
        canceledContract.setTitle("Old Streaming Sub");
        canceledContract.setContractor(contractor);
        canceledContract.setFee(9.99f);
        canceledContract.setActive(true);
        canceledContract.setStartDate(LocalDate.now().minusMonths(12));
        canceledContract.setInitialEndDate(LocalDate.now().minusMonths(1));
        canceledContract.setCanceled(true);
        canceledContract.setCancellationDate(LocalDate.now().minusDays(5));
        canceledContract.setTerminationDate(LocalDate.now().plusDays(25));
        canceledContract = contractRepository.saveAndFlush(canceledContract);
        entityManager.refresh(canceledContract);
        assertEquals(Contract.ContractStatus.CANCELED_PENDING, canceledContract.getCalculatedStatus());
        assertNotNull(canceledContract.getDaysRemaining());
        assertTrue(canceledContract.getDaysRemaining() > 0, "Canceled pending contract must have positive days remaining to termination");

        // 4. Inactive / terminated contract
        Contract inactiveContract = new Contract();
        inactiveContract.setTitle("Terminated Gym");
        inactiveContract.setContractor(contractor);
        inactiveContract.setFee(45.00f);
        inactiveContract.setActive(false);
        inactiveContract.setStartDate(LocalDate.now().minusYears(3));
        inactiveContract.setInitialEndDate(LocalDate.now().minusYears(2));
        inactiveContract = contractRepository.saveAndFlush(inactiveContract);
        entityManager.refresh(inactiveContract);
        assertEquals(Contract.ContractStatus.TERMINATED, inactiveContract.getCalculatedStatus());
        assertEquals(0, inactiveContract.getDaysRemaining(), "Terminated contract must have 0 days remaining");
    }
}
