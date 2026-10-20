package de.oberamsystems.slm;

import de.oberamsystems.slm.model.Employer;
import de.oberamsystems.slm.model.EmployerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class EmployerIT extends AbstractIntegrationTest {

    @Autowired
    private EmployerRepository employerRepository;

    @Test
    public void testEmployerPersistenceAndSorting() {
        // Test Employer directory sorting
        Employer e2 = employerRepository.saveAndFlush(new Employer("Zeta Consulting"));
        Employer e1 = employerRepository.saveAndFlush(new Employer("Alpha Tech"));

        assertNotNull(e1.getId());
        assertNotNull(e2.getId());

        List<Employer> sortedEmployers = employerRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        assertTrue(sortedEmployers.size() >= 2);
        int alphaIdx = -1;
        int zetaIdx = -1;
        for (int i = 0; i < sortedEmployers.size(); i++) {
            if ("Alpha Tech".equals(sortedEmployers.get(i).getName())) alphaIdx = i;
            if ("Zeta Consulting".equals(sortedEmployers.get(i).getName())) zetaIdx = i;
        }
        assertTrue(alphaIdx < zetaIdx, "Employers must be sorted alphabetically ascending");
    }
}
