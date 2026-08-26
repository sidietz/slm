package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PurchaseControllerTest {

    @Test
    public void testPurchaseController() throws Exception {
        PurchaseController controller = new PurchaseController();
        
        PurchaseRepository repo = mock(PurchaseRepository.class);
        VendorRepository vendorRepo = mock(VendorRepository.class);
        
        java.lang.reflect.Field f1 = PurchaseController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = PurchaseController.class.getDeclaredField("vendorRepo");
        f2.setAccessible(true);
        f2.set(controller, vendorRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        List<Purchase> purchases = new ArrayList<>();
        Purchase p1 = new Purchase();
        p1.setPrice(10.5f);
        purchases.add(p1);
        when(repo.findAll(any(Sort.class))).thenReturn(purchases);
        
        // purchases
        String view1 = controller.addPurchases(1L, model);
        assertEquals("purchases", view1);
        assertEquals(10.5f, model.getAttribute("totalPrice"));
        
        Purchase p2 = new Purchase();
        String view2 = controller.submitPurchases(p2, model);
        assertEquals("redirect:/purchases", view2);
    }
}
