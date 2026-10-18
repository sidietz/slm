package de.oberamsystems.slm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class SelfieStorageServiceTest {

    @TempDir
    Path tempDir;

    private SelfieStorageService storageService;

    @BeforeEach
    public void setup() throws IOException {
        storageService = new SelfieStorageService(tempDir.toString());
        storageService.init();
    }

    @Test
    public void testStoreJpgUntouched() throws IOException {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01, 0x02};
        MockMultipartFile file = new MockMultipartFile("file", "myphoto.jpg", "image/jpeg", content);

        LocalDate date = LocalDate.of(2026, 10, 18);
        Path stored = storageService.storeSelfie(file, date, null);

        assertTrue(Files.exists(stored));
        assertEquals("2026-10-18.jpg", stored.getFileName().toString());
        assertArrayEquals(content, Files.readAllBytes(stored), "Raw bytes must be stored without conversion");
        assertTrue(stored.startsWith(tempDir));
    }

    @Test
    public void testStoreHeicUntouched() throws IOException {
        byte[] content = new byte[]{0x00, 0x00, 0x00, 0x18, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c'};
        MockMultipartFile file = new MockMultipartFile("file", "iphone_snapshot.heic", "image/heic", content);

        LocalDate date = LocalDate.of(2026, 10, 19);
        Path stored = storageService.storeSelfie(file, date, null);

        assertTrue(Files.exists(stored));
        assertEquals("2026-10-19.heic", stored.getFileName().toString());
        assertArrayEquals(content, Files.readAllBytes(stored), "HEIC bytes must be stored directly without conversion");
        assertTrue(stored.startsWith(tempDir));
    }

    @Test
    public void testCaseInsensitiveExtensions() throws IOException {
        byte[] content = new byte[]{1, 2, 3};
        MockMultipartFile fileUpperJpg = new MockMultipartFile("file", "PHOTO.JPG", "image/jpeg", content);
        Path stored1 = storageService.storeSelfie(fileUpperJpg, LocalDate.of(2026, 10, 20), null);
        assertEquals("2026-10-20.jpg", stored1.getFileName().toString());

        MockMultipartFile fileUpperHeic = new MockMultipartFile("file", "PHOTO.HEIC", "image/heic", content);
        Path stored2 = storageService.storeSelfie(fileUpperHeic, LocalDate.of(2026, 10, 21), null);
        assertEquals("2026-10-21.heic", stored2.getFileName().toString());

        MockMultipartFile fileJpeg = new MockMultipartFile("file", "photo.jpeg", "image/jpeg", content);
        Path stored3 = storageService.storeSelfie(fileJpeg, LocalDate.of(2026, 10, 22), null);
        assertEquals("2026-10-22.jpeg", stored3.getFileName().toString());
    }

    @Test
    public void testRejectPathTraversalInFilename() {
        byte[] content = new byte[]{1, 2, 3};

        MockMultipartFile traversalFile1 = new MockMultipartFile("file", "../../evil.jpg", "image/jpeg", content);
        assertThrows(SecurityException.class, () -> storageService.validateAndExtractExtension(traversalFile1));

        MockMultipartFile traversalFile2 = new MockMultipartFile("file", "..\\..\\evil.heic", "image/heic", content);
        assertThrows(SecurityException.class, () -> storageService.validateAndExtractExtension(traversalFile2));

        MockMultipartFile traversalFile3 = new MockMultipartFile("file", "/etc/passwd.jpg", "image/jpeg", content);
        assertThrows(SecurityException.class, () -> storageService.validateAndExtractExtension(traversalFile3));

        MockMultipartFile traversalFile4 = new MockMultipartFile("file", "foo/bar/test.heic", "image/heic", content);
        assertThrows(SecurityException.class, () -> storageService.validateAndExtractExtension(traversalFile4));
    }

    @Test
    public void testResolveSecurePathRejectsEscape() {
        assertThrows(SecurityException.class, () -> storageService.resolveSecurePath("../outside.jpg"));
        assertThrows(SecurityException.class, () -> storageService.resolveSecurePath("..\\outside.jpg"));
        assertThrows(SecurityException.class, () -> storageService.resolveSecurePath("nested/inside.jpg"));
        assertThrows(SecurityException.class, () -> storageService.resolveSecurePath("nullbyte\0.jpg"));
    }

    @Test
    public void testRejectInvalidExtensions() {
        byte[] content = new byte[]{1, 2, 3};

        MockMultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf", content);
        assertThrows(IllegalArgumentException.class, () -> storageService.validateAndExtractExtension(pdfFile));

        MockMultipartFile shFile = new MockMultipartFile("file", "script.sh", "text/plain", content);
        assertThrows(IllegalArgumentException.class, () -> storageService.validateAndExtractExtension(shFile));

        MockMultipartFile pngFile = new MockMultipartFile("file", "image.png", "image/png", content);
        assertThrows(IllegalArgumentException.class, () -> storageService.validateAndExtractExtension(pngFile));
    }

    @Test
    public void testCleanupPreviousFileOnFormatChange() throws IOException {
        byte[] content1 = new byte[]{1, 2, 3};
        LocalDate date = LocalDate.of(2026, 10, 23);
        MockMultipartFile jpgFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content1);
        Path firstStored = storageService.storeSelfie(jpgFile, date, null);
        assertTrue(Files.exists(firstStored));

        // Now upload .heic for the same date
        byte[] content2 = new byte[]{4, 5, 6};
        MockMultipartFile heicFile = new MockMultipartFile("file", "photo.heic", "image/heic", content2);
        Path secondStored = storageService.storeSelfie(heicFile, date, firstStored.toString());

        assertTrue(Files.exists(secondStored));
        assertFalse(Files.exists(firstStored), "Previous .jpg file should be removed when format changes to .heic");
    }
}
