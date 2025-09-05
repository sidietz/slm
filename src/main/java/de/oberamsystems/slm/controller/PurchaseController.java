package de.oberamsystems.slm.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.VendorRepository;
import de.oberamsystems.slm.model.Purchase;
import de.oberamsystems.slm.model.PurchaseRepository;
import de.oberamsystems.slm.model.Vendor;


@Controller
public class PurchaseController {

	Logger log = LoggerFactory.getLogger(PurchaseController.class);
	
	@Autowired
	private PurchaseRepository repo;
	
	@Autowired
	private VendorRepository vendorRepo;
	
	@GetMapping("/purchase")
	public String getpurchase(Model model) {
		
		List<Purchase> purchases = repo.findAll();
		float totalPrice = 0;
		
		for (Purchase p : purchases) {
			totalPrice = totalPrice + p.getPrice();
		}

		model.addAttribute("purchases", purchases);
		model.addAttribute("totalPrice", totalPrice);
		return "purchase";
	}
	
	@GetMapping({"/vendor.html", "/vendors.html", "/vendor", "/vendors"})
	public String index(Model model) {
		List<Vendor> tss = vendorRepo.findAll();
		model.addAttribute("vendors", tss);
		return "vendor";
	}
	
	@GetMapping("/add-purchase")
	public String addPurchase(@RequestParam(required = false) Long id, Model model) {
		Purchase p = new Purchase();
		p.setPurchaseDate(LocalDate.now());
		model.addAttribute("purchase", new Purchase());
		model.addAttribute("vendors", vendorRepo.findAll());
		return "add-purchase";
	}
	
	@PostMapping("/add-purchase")
	public String submitPurchase(@ModelAttribute Purchase tt, Model model) {
		model.addAttribute("vendors", vendorRepo.findAll());
		repo.save(tt);
		model.addAttribute("purchase", tt);
		return "add-purchase";
	}
	
	@GetMapping("/add-vendor")
	public String addVendor(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("vendor", new Vendor());
		return "add-vendor";
	}
	
	@PostMapping("/add-vendor")
	public String submitVendor(@ModelAttribute Vendor ts, Model model) {
		model.addAttribute("vendors", vendorRepo.findAll());
		vendorRepo.save(ts);
		model.addAttribute("vendor", ts);
		return "add-vendor";
	}
	
	
	
	
}
