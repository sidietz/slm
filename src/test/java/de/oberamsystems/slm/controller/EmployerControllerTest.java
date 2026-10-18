package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.Employer;
import de.oberamsystems.slm.model.EmployerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class EmployerControllerTest {

    @Test
    public void testEmployerController() throws Exception {
        EmployerController controller = new EmployerController();
        EmployerRepository employerRepo = mock(EmployerRepository.class);

        java.lang.reflect.Field field = EmployerController.class.getDeclaredField("employerRepo");
        field.setAccessible(true);
        field.set(controller, employerRepo);

        List<Employer> employers = new ArrayList<>();
        employers.add(new Employer(1L, "Acme Corp"));
        when(employerRepo.findAll()).thenReturn(employers);
        when(employerRepo.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Sort.class))).thenReturn(employers);

        ConcurrentModel model = new ConcurrentModel();

        // GET /employers
        String view1 = controller.addEmployers(null, model);
        assertEquals("employers", view1);
        assertEquals(employers, model.getAttribute("employers"));
        assertNotNull(model.getAttribute("employer"));

        // POST /employers
        Employer newEmp = new Employer("Oberam Systems");
        String view2 = controller.submitEmployers(newEmp, model);
        assertEquals("redirect:/employers", view2);
        verify(employerRepo).save(newEmp);
    }
}
