package de.oberamsystems.slm.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.oberamsystems.slm.model.Selfie;
import de.oberamsystems.slm.model.SelfieRepository;
import de.oberamsystems.slm.service.SelfieStorageService;

@Controller
public class SelfieController {

	private static final Logger log = LoggerFactory.getLogger(SelfieController.class);

	@Autowired
	private SelfieRepository selfieRepository;

	@Autowired
	private SelfieStorageService selfieStorageService;

	@GetMapping("/selfies")
	public String listSelfies(Model model) {
		model.addAttribute("selfies", selfieRepository.findAll(Sort.by(Sort.Direction.DESC, "date")));
		model.addAttribute("selfie", new Selfie());
		return "selfies";
	}

	@PostMapping("/selfies")
	public String uploadSelfie(@RequestParam("file") MultipartFile file, 
							   @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		try {
			Selfie selfie = selfieRepository.findByDate(date);
			String previousLocation = selfie != null ? selfie.getLocation() : null;

			Path filepath = selfieStorageService.storeSelfie(file, date, previousLocation);

			if (selfie == null) {
				selfie = new Selfie();
			}
			selfie.setDate(date);
			selfie.setLocation(filepath.toString());
			selfieRepository.save(selfie);

			return "redirect:/selfies?success=Selfie uploaded successfully.";
		} catch (SecurityException e) {
			log.warn("Security rejection during selfie upload: {}", e.getMessage());
			return "redirect:/selfies?error=Security violation: Path traversal or invalid characters detected.";
		} catch (IllegalArgumentException e) {
			return "redirect:/selfies?error=" + e.getMessage();
		} catch (IOException e) {
			log.error("Failed to upload selfie file", e);
			return "redirect:/selfies?error=Failed to upload file.";
		}
	}
}
