package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class FinanceManagementIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testEmployerFlow() throws Exception {
        // 1. GET /employers
        mockMvc.perform(get("/employers"))
                .andExpect(status().isOk())
                .andExpect(view().name("employers"))
                .andExpect(model().attributeExists("employers", "employer"));

        // 2. POST /employers
        mockMvc.perform(post("/employers")
                        .with(csrf())
                        .param("name", "JetBrains s.r.o."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employers"));

        assertEquals(1, employerRepository.count());
        Employer employer = employerRepository.findAll().getFirst();
        assertEquals("JetBrains s.r.o.", employer.getName());

        // 3. GET /employers with content
        mockMvc.perform(get("/employers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("JetBrains s.r.o.")));
    }

    @Test
    void testContractorAndContractFlow() throws Exception {
        // 1. Contractors: GET /contractors & POST /contractors
        mockMvc.perform(get("/contractors"))
                .andExpect(status().isOk())
                .andExpect(view().name("contractors"))
                .andExpect(model().attributeExists("contractors", "contractor"));

        mockMvc.perform(post("/contractors")
                        .with(csrf())
                        .param("name", "Deutsche Telekom AG"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contractors"));

        assertEquals(1, contractorRepository.count());
        Contractor contractor = contractorRepository.findAll().getFirst();

        // 2. Contracts: GET /contracts
        mockMvc.perform(get("/contracts"))
                .andExpect(status().isOk())
                .andExpect(view().name("contracts"))
                .andExpect(model().attributeExists("contracts", "totalFee", "contract", "contractors"))
                .andExpect(content().string(containsString("Deutsche Telekom AG")));

        // 3. Contracts: POST /contracts
        mockMvc.perform(post("/contracts")
                        .with(csrf())
                        .param("title", "Fiber Internet 500M")
                        .param("contractor", String.valueOf(contractor.getId()))
                        .param("fee", "49.95")
                        .param("startDate", "2026-01-01")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contracts"));

        assertEquals(1, contractRepository.count());
        Contract contract = contractRepository.findAll().getFirst();
        assertEquals("Fiber Internet 500M", contract.getTitle());
        assertEquals(49.95f, contract.getFee(), 0.001);
    }

    @Test
    void testPurchaseFlow() throws Exception {
        Vendor vendor = new Vendor();
        vendor.setName("Cyberport Munich");
        vendor = vendorRepository.saveAndFlush(vendor);

        // 1. GET /purchases
        mockMvc.perform(get("/purchases"))
                .andExpect(status().isOk())
                .andExpect(view().name("purchases"))
                .andExpect(model().attributeExists("purchases", "totalPrice", "purchase", "vendors"))
                .andExpect(content().string(containsString("Cyberport Munich")));

        // 2. POST /purchases
        mockMvc.perform(post("/purchases")
                        .with(csrf())
                        .param("title", "USB-C NVMe Enclosure")
                        .param("vendor", String.valueOf(vendor.getId()))
                        .param("price", "39.99")
                        .param("purchaseDate", "2026-10-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/purchases"));

        assertEquals(1, purchaseRepository.count());
        Purchase purchase = purchaseRepository.findAll().getFirst();
        assertEquals("USB-C NVMe Enclosure", purchase.getTitle());
        assertEquals(39.99f, purchase.getPrice(), 0.001);
    }

    @Test
    void testUpdateEmployerFlow() throws Exception {
        Employer employer = new Employer();
        employer.setName("Old Tech Corp");
        employer = employerRepository.saveAndFlush(employer);
        Long empId = employer.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/employers")
                        .with(csrf())
                        .param("id", empId.toString())
                        .param("name", "New Tech Holding GmbH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employers"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, employerRepository.count(), "Editing employer must not create duplicate row");
        Employer updated = employerRepository.findById(empId).orElseThrow();
        assertEquals("New Tech Holding GmbH", updated.getName());
    }

    @Test
    void testUpdateContractorFlow() throws Exception {
        Contractor contractor = new Contractor();
        contractor.setName("Telekom Deutschland GmbH");
        contractor = contractorRepository.saveAndFlush(contractor);
        Long contractorId = contractor.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/contractors")
                        .with(csrf())
                        .param("id", contractorId.toString())
                        .param("name", "Deutsche Telekom AG"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contractors"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, contractorRepository.count(), "Editing contractor must not create duplicate row");
        Contractor updated = contractorRepository.findById(contractorId).orElseThrow();
        assertEquals("Deutsche Telekom AG", updated.getName());
    }

    @Test
    void testUpdateVendorFlow() throws Exception {
        Vendor vendor = new Vendor();
        vendor.setName("Amazon DE");
        vendor = vendorRepository.saveAndFlush(vendor);
        Long vendorId = vendor.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/vendors")
                        .with(csrf())
                        .param("id", vendorId.toString())
                        .param("name", "Amazon EU S.a.r.l."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendors"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, vendorRepository.count(), "Editing vendor must not create duplicate row");
        Vendor updated = vendorRepository.findById(vendorId).orElseThrow();
        assertEquals("Amazon EU S.a.r.l.", updated.getName());
    }

    @Test
    void testUpdatePurchaseFlow() throws Exception {
        Vendor v1 = new Vendor();
        v1.setName("Saturn Electronics");
        v1 = vendorRepository.saveAndFlush(v1);

        Vendor v2 = new Vendor();
        v2.setName("MediaMarkt");
        v2 = vendorRepository.saveAndFlush(v2);

        Purchase purchase = new Purchase();
        purchase.setTitle("Logitech MX Master 3S");
        purchase.setVendor(v1);
        purchase.setPrice(99.99f);
        purchase.setPurchaseDate(LocalDate.of(2026, 10, 10));
        purchase = purchaseRepository.saveAndFlush(purchase);
        Long purchaseId = purchase.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/purchases")
                        .with(csrf())
                        .param("id", purchaseId.toString())
                        .param("title", "Logitech MX Master 3S - Space Grey")
                        .param("vendor", v2.getId().toString())
                        .param("price", "89.95")
                        .param("purchaseDate", "2026-10-12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/purchases"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, purchaseRepository.count(), "Editing purchase must not create duplicate row");
        Purchase updated = purchaseRepository.findById(purchaseId).orElseThrow();
        assertEquals("Logitech MX Master 3S - Space Grey", updated.getTitle());
        assertEquals(v2.getId(), updated.getVendor().getId());
        assertEquals(89.95f, updated.getPrice(), 0.001);
        assertEquals(LocalDate.of(2026, 10, 12), updated.getPurchaseDate());
    }
}
