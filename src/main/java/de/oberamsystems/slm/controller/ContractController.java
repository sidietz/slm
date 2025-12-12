package de.oberamsystems.slm.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.ContractRepository;
import de.oberamsystems.slm.model.Contractor;
import de.oberamsystems.slm.model.ContractorRepository;
import de.oberamsystems.slm.model.Contract;

@Controller
public class ContractController {

	Logger log = LoggerFactory.getLogger(ContractController.class);
	
	@Autowired
	private ContractRepository repo;
	
	@Autowired
	private ContractorRepository contractorRepo;
	
	@GetMapping("/contracts")
	public String addContracts(@RequestParam(required = false) Long id, Model model) {
		List<Contract> contracts = repo.findAll();
		float totalPrice = 0;
		
		for (Contract p : contracts) {
			totalPrice = totalPrice + p.getFee();
		}

		model.addAttribute("contracts", contracts);
		model.addAttribute("totalFee", totalPrice);
		Contract p = new Contract();
		p.setStartDate(LocalDate.now());
		p.setActive(true);
		model.addAttribute("contract", p);
		model.addAttribute("contractors", contractorRepo.findAll());
		return "contracts";
	}
	
	@PostMapping("/contracts")
	public String submitContracts(@ModelAttribute Contract tt, Model model) {
		List<Contract> contracts = repo.findAll();
		float totalPrice = 0;
		
		for (Contract p : contracts) {
			totalPrice = totalPrice + p.getFee();
		}

		model.addAttribute("contracts", contracts);
		model.addAttribute("totalFee", totalPrice);
		model.addAttribute("contractors", contractorRepo.findAll());
		repo.save(tt);
		model.addAttribute("contract", tt);
		return "redirect:/contracts";
	}
	
	@GetMapping("/contractors")
	public String addContractors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("contractors", contractorRepo.findAll());
		model.addAttribute("contractor", new Contractor());
		return "contractors";
	}
	
	@PostMapping("/contractors")
	public String submitContractors(@ModelAttribute Contractor ts, Model model) {
		model.addAttribute("contractors", contractorRepo.findAll());
		contractorRepo.save(ts);
		model.addAttribute("contractor", ts);
		return "redirect:/contractors";
	}
}
