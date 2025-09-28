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
	
	@GetMapping("/contract")
	public String getContract(Model model) {
		
		List<Contract> contracts = repo.findAll();
		float totalPrice = 0;
		
		for (Contract p : contracts) {
			totalPrice = totalPrice + p.getFee();
		}

		model.addAttribute("contracts", contracts);
		model.addAttribute("totalFee", totalPrice);
		return "contract";
	}
	
	@GetMapping({"/contractor.html", "/contractors.html", "/contractor", "/contractors"})
	public String contractor(Model model) {
		List<Contractor> tss = contractorRepo.findAll();
		model.addAttribute("contractors", tss);
		return "contractor";
	}
	
	@GetMapping("/add-contract")
	public String addContract(@RequestParam(required = false) Long id, Model model) {
		Contract p = new Contract();
		p.setStartDate(LocalDate.now());
		model.addAttribute("contract", new Contract());
		model.addAttribute("contractors", contractorRepo.findAll());
		return "add-contract";
	}
	
	@PostMapping("/add-contract")
	public String submitContract(@ModelAttribute Contract tt, Model model) {
		model.addAttribute("contractors", contractorRepo.findAll());
		repo.save(tt);
		model.addAttribute("contract", tt);
		return "add-contract";
	}
	
	@GetMapping("/add-contractor")
	public String addcontractor(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("contractor", new Contractor());
		return "add-contractor";
	}
	
	@PostMapping("/add-contractor")
	public String submitcontractor(@ModelAttribute Contractor ts, Model model) {
		model.addAttribute("contractors", contractorRepo.findAll());
		contractorRepo.save(ts);
		model.addAttribute("contractor", ts);
		return "add-contractor";
	}
}
