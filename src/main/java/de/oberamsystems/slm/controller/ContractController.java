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

import org.springframework.data.domain.Sort;

@Controller
public class ContractController {

	Logger log = LoggerFactory.getLogger(ContractController.class);
	
	@Autowired
	private ContractRepository repo;
	
	@Autowired
	private ContractorRepository contractorRepo;
	
	@GetMapping("/contracts")
	public String addContracts(@RequestParam(required = false) Long id, Model model) {
		List<Contract> contracts = repo.findAll(Sort.by(Sort.Direction.DESC, "startDate"));
		float totalPrice = 0;
		
		for (Contract p : contracts) {
			if (p.isActive()) {
				totalPrice = totalPrice + p.getFee();
			}
		}

		model.addAttribute("contracts", contracts);
		model.addAttribute("totalFee", totalPrice);
		Contract p = new Contract();
		p.setStartDate(LocalDate.now());
		p.setInitialEndDate(LocalDate.now().plusYears(1));
		p.setAutoExtension(true);
		p.setAutoExtensionMonths(1);
		p.setActive(true);
		p.setCanceled(false);
		model.addAttribute("contract", p);
		model.addAttribute("contractors", contractorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		return "contracts";
	}
	
	@PostMapping("/contracts")
	public String submitContracts(@ModelAttribute Contract tt, Model model) {
		if (tt.isCanceled() && tt.getTerminationDate() == null) {
			tt.setTerminationDate(tt.getInitialEndDate() != null ? tt.getInitialEndDate() : LocalDate.now());
		}
		repo.save(tt);
		return "redirect:/contracts";
	}
	
	@GetMapping("/contractors")
	public String addContractors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("contractors", contractorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("contractor", new Contractor());
		return "contractors";
	}
	
	@PostMapping("/contractors")
	public String submitContractors(@ModelAttribute Contractor ts, Model model) {
		model.addAttribute("contractors", contractorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		contractorRepo.save(ts);
		model.addAttribute("contractor", ts);
		return "redirect:/contractors";
	}
}
