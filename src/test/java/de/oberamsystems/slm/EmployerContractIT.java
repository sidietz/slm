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
    }
}
