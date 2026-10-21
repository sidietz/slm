package de.oberamsystems.slm.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.VendorRepository;
import de.oberamsystems.slm.model.Purchase;
import de.oberamsystems.slm.model.PurchaseRepository;
import de.oberamsystems.slm.model.Vendor;


import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PurchaseController {

	Logger log = LoggerFactory.getLogger(PurchaseController.class);
	
	@Autowired
	private PurchaseRepository repo;
	
	@Autowired
	private VendorRepository vendorRepo;
	
	@GetMapping("/purchases")
	public String addPurchases(@RequestParam(required = false) Long id, Model model) {
		List<Purchase> purchases = repo.findAll(Sort.by(Sort.Direction.DESC, "purchaseDate"));
		float totalPrice = 0;
		
		for (Purchase p : purchases) {
			totalPrice = totalPrice + p.getPrice();
		}

		model.addAttribute("purchases", purchases);
		model.addAttribute("totalPrice", totalPrice);
		Purchase p = new Purchase();
		p.setPurchaseDate(LocalDate.now());
		model.addAttribute("purchase", p);
		model.addAttribute("vendors", vendorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		return "purchases";
	}
	
	public String submitPurchases(Purchase tt, Model model) {
		return submitPurchases(tt, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/purchases")
	public String submitPurchases(@ModelAttribute Purchase tt, Model model, RedirectAttributes redirectAttributes) {
		if (tt.getVendor() == null || tt.getVendor().getId() == null || !vendorRepo.existsById(tt.getVendor().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vendor is required for purchase.");
			return "redirect:/purchases";
		}
		if (tt.getId() != null && (tt.getId() <= 0 || !repo.existsById(tt.getId()))) {
			tt.setId(null);
		}
		repo.save(tt);
		return "redirect:/purchases";
	}
}
