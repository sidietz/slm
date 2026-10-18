package de.oberamsystems.slm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class SelfieStorageService {

    private static final Logger log = LoggerFactory.getLogger(SelfieStorageService.class);

    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "heic");
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,4}$");

    private final Path baseDir;

    public SelfieStorageService(@Value("${slm.selfies.dir:${user.dir}/selfies}") String selfiesDir) {
        this.baseDir = Paths.get(selfiesDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public String validateAndExtractExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file is empty or missing.");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Invalid or missing filename.");
        }

        // Clean path and reject path traversal sequences
        String cleanName = StringUtils.cleanPath(originalFilename);
        if (cleanName.contains("..") || cleanName.contains("/") || cleanName.contains("\\") || cleanName.contains("\0")) {
            log.warn("Path traversal sequence in upload filename: {}", originalFilename);
            throw new SecurityException("Invalid filename: Path traversal characters are not permitted.");
        }

        String ext = StringUtils.getFilenameExtension(cleanName);
        if (ext == null || !EXTENSION_PATTERN.matcher(ext).matches()) {
            throw new IllegalArgumentException("Invalid file extension.");
        }

        String lowerExt = ext.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(lowerExt)) {
            throw new IllegalArgumentException("Invalid file format. Only .jpg, .jpeg, or .heic files are allowed.");
        }
        return lowerExt;
    }

    public Path resolveSecurePath(String relativeFilename) {
        if (relativeFilename == null || relativeFilename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be blank.");
        }

        if (relativeFilename.contains("..") || relativeFilename.contains("/") || 
            relativeFilename.contains("\\") || relativeFilename.contains("\0")) {
            log.warn("Path traversal sequence in relative filename: {}", relativeFilename);
            throw new SecurityException("Path traversal attempt detected: " + relativeFilename);
        }

        Path targetPath = baseDir.resolve(relativeFilename).normalize().toAbsolutePath();
        if (!targetPath.startsWith(baseDir)) {
            log.warn("Resolved path {} escapes base directory {}", targetPath, baseDir);
            throw new SecurityException("Path traversal attempt detected: " + relativeFilename);
        }

        return targetPath;
    }

    /**
     * Stores the raw byte contents of the selfie file as-is without any conversion.
     */
    public Path storeSelfie(MultipartFile file, LocalDate date, String previousLocation) throws IOException {
        if (date == null) {
            throw new IllegalArgumentException("Selfie date cannot be null.");
        }

        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }

        String ext = validateAndExtractExtension(file);
        String filename = date.toString() + "." + ext;
        Path targetPath = resolveSecurePath(filename);

        // Clean up previous file if format changed (e.g. from .jpg to .heic)
        if (previousLocation != null && !previousLocation.isBlank()) {
            try {
                Path prevPath = Paths.get(previousLocation).normalize().toAbsolutePath();
                if (prevPath.startsWith(baseDir) && !prevPath.equals(targetPath)) {
                    Files.deleteIfExists(prevPath);
                }
            } catch (Exception e) {
                log.debug("Could not delete previous selfie file: {}", e.getMessage());
            }
        }

        // Write raw bytes untouched
        Files.write(targetPath, file.getBytes());
        return targetPath;
    }
}
