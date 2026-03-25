package de.oberamsystems.slm.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.oberamsystems.slm.model.Selfie;
import de.oberamsystems.slm.model.SelfieRepository;

@Controller
public class SelfieController {

	@Autowired
	private SelfieRepository selfieRepository;

    @GetMapping("/selfies")
    public String listSelfies(Model model) {
		model.addAttribute("selfies", selfieRepository.findAll());
		model.addAttribute("selfie", new Selfie());
        return "selfies";
    }

	@PostMapping("/selfies")
	public String uploadSelfie(@RequestParam("file") MultipartFile file, 
							   @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".jpg")) {
			return "redirect:/selfies?error=Invalid file. Only .jpg files are allowed.";
		}

		try {
			String uploadDir = System.getProperty("user.dir") + "/selfies";
			File dir = new File(uploadDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			String filename = date.toString() + ".jpg";
			Path filepath = Paths.get(uploadDir, filename);
			Files.write(filepath, file.getBytes());

			Selfie selfie = selfieRepository.findByDate(date);
			if (selfie == null) {
				selfie = new Selfie();
			}
			selfie.setDate(date);
			selfie.setLocation(filepath.toString());
			selfieRepository.save(selfie);

		} catch (IOException e) {
			e.printStackTrace();
			return "redirect:/selfies?error=Failed to upload file.";
		}

		return "redirect:/selfies?success=Selfie uploaded successfully.";
	}
}
