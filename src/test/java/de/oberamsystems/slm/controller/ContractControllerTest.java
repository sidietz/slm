package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContractControllerTest {

    @Test
    public void testContractController() throws Exception {
        ContractController controller = new ContractController();
        
        ContractRepository repo = mock(ContractRepository.class);
        ContractorRepository contractorRepo = mock(ContractorRepository.class);
        
        java.lang.reflect.Field f1 = ContractController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = ContractController.class.getDeclaredField("contractorRepo");
        f2.setAccessible(true);
        f2.set(controller, contractorRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        List<Contract> contracts = new ArrayList<>();
        Contract c1 = new Contract();
        c1.setFee(10.5f);
        contracts.add(c1);
        when(repo.findAll()).thenReturn(contracts);
        
        // contracts
        String view1 = controller.addContracts(1L, model);
        assertEquals("contracts", view1);
        assertEquals(10.5f, model.getAttribute("totalFee"));
        
        Contract c2 = new Contract();
        String view2 = controller.submitContracts(c2, model);
        assertEquals("redirect:/contracts", view2);
        
        // contractors
        String view3 = controller.addContractors(1L, model);
        assertEquals("contractors", view3);
        
        Contractor c3 = new Contractor();
        String view4 = controller.submitContractors(c3, model);
        assertEquals("redirect:/contractors", view4);
    }
}
