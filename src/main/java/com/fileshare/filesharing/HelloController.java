
package com.fileshare.filesharing;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@CrossOrigin(origins = "*")
@RestController
public class HelloController {
    private final Path uploadDir = Paths.get("uploads");
    private final Map<String, Map<String, Object>> sharedFiles = new ConcurrentHashMap<>();

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello! File Sharing System is working!";
    }

    @PostMapping("/api/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                         HttpSession session) throws IOException {
    checkLogin(session);
        if (file.isEmpty()) {
            return "Please select a file!";
        }

        Files.createDirectories(uploadDir);

        String fileName = Paths.get(file.getOriginalFilename())
                .getFileName().toString();

        Path filePath = uploadDir.resolve(fileName);

        Files.copy(file.getInputStream(), filePath,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return "File saved successfully: " + fileName;
    }

    @GetMapping("/api/download")
    public ResponseEntity<Resource> downloadFile(
        @RequestParam("filename") String filename,
        HttpSession session) throws IOException {
    checkLogin(session);
        String safeFileName = Paths.get(filename).getFileName().toString();
        Path filePath = uploadDir.resolve(safeFileName);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + safeFileName + "\"")
        .body(resource);
    }

    
@PostMapping("/api/share")
public ResponseEntity<String> shareFile(
        @RequestParam("filename") String filename,
        @RequestParam("password") String password,
        HttpSession session) {

    checkLogin(session);

    if (password == null || password.length() < 6) {
        return ResponseEntity.badRequest()
                .body("Password must be at least 6 characters");
    }

    String safeFileName = Paths.get(filename).getFileName().toString();
    Path filePath = uploadDir.resolve(safeFileName);

    if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
        return ResponseEntity.notFound().build();
    }

    String token = UUID.randomUUID().toString();

    LocalDateTime expiryTime =
            LocalDateTime.now().plus(24, ChronoUnit.HOURS);

    Map<String, Object> fileDetails = new ConcurrentHashMap<>();
    fileDetails.put("filename", safeFileName);
    fileDetails.put("expiryTime", expiryTime);
    fileDetails.put("password", passwordEncoder.encode(password));

    sharedFiles.put(token, fileDetails);

    return ResponseEntity.ok(
            "http://localhost:8081/api/share/" + token);
}

    private final BCryptPasswordEncoder passwordEncoder =
        new BCryptPasswordEncoder();
    private void checkLogin(HttpSession session) {
    if (session.getAttribute("loggedInUser") == null) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please login first"
        );
    }
}

    
@GetMapping("/api/share/{token}")
public ResponseEntity<String> showSharePage(
        @org.springframework.web.bind.annotation.PathVariable String token) {

    Map<String, Object> fileDetails = sharedFiles.get(token);

    if (fileDetails == null) {
        return ResponseEntity.notFound().build();
    }

    LocalDateTime expiryTime =
            (LocalDateTime) fileDetails.get("expiryTime");

    if (LocalDateTime.now().isAfter(expiryTime)) {
        sharedFiles.remove(token);
        return ResponseEntity.notFound().build();
    }

    String html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Secure FileShare</title>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
                * { box-sizing: border-box; }
                body {
                    margin: 0;
                    font-family: Arial, sans-serif;
                    background: linear-gradient(135deg, #f4efff, #e9eaff);
                    min-height: 100vh;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                .card {
                    background: white;
                    width: 90%;
                    max-width: 420px;
                    padding: 40px 30px;
                    border-radius: 22px;
                    text-align: center;
                    box-shadow: 0 15px 45px rgba(80,50,140,.15);
                }
                h1 { color: #7955d9; }
                p { color: #77718e; line-height: 1.5; }
                input {
                    width: 100%;
                    padding: 14px;
                    margin: 18px 0;
                    border: 1px solid #ddd3f5;
                    border-radius: 10px;
                    font-size: 15px;
                }
                button {
                    width: 100%;
                    padding: 14px;
                    background: #7955d9;
                    color: white;
                    border: none;
                    border-radius: 10px;
                    font-size: 15px;
                    cursor: pointer;
                }
                button:hover { background: #6240bd; }
                #message { margin-top: 18px; }
            </style>
        </head>
        <body>
            <div class="card">
                <h1>🔐 FileShare</h1>
                <h2>Protected File</h2>
                <p>This file is password protected.
                   Enter the password to download it.</p>

                <form id="unlockForm">
                    <input type="password" id="password"
                        placeholder="Enter share password" required>
                    <button type="submit">Unlock & Download</button>
                </form>

                <p id="message"></p>
            </div>

            <script>
                document.getElementById("unlockForm")
                .addEventListener("submit", async function(event) {
                    event.preventDefault();

                    const password =
                        document.getElementById("password").value;
                    const message =
                        document.getElementById("message");

                    message.textContent = "Verifying password...";

                    try {
                        const response = await fetch(
                            "/api/share/SHARE_TOKEN/download",
                            {
                                method: "POST",
                                headers: {
                                    "Content-Type":
                                        "application/x-www-form-urlencoded"
                                },
                                body: new URLSearchParams({ password })
                            }
                        );

                        if (!response.ok) {
                            message.textContent =
                                response.status === 401
                                ? "Incorrect password. Try again."
                                : "Link expired or file unavailable.";
                            return;
                        }

                        const blob = await response.blob();
                        const url = URL.createObjectURL(blob);
                        const link = document.createElement("a");
                        link.href = url;
                        link.download = "shared-file";
                        link.click();
                        URL.revokeObjectURL(url);

                        message.textContent = "Download started!";
                    } catch (error) {
                        message.textContent = "Something went wrong.";
                    }
                });
            </script>
        </body>
        </html>
        """.replace("SHARE_TOKEN", token);

    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
}


@PostMapping("/api/share/{token}/download")
public ResponseEntity<Resource> downloadSharedFile(
        @org.springframework.web.bind.annotation.PathVariable String token,
        @RequestParam("password") String password) throws IOException {

    Map<String, Object> fileDetails = sharedFiles.get(token);

    if (fileDetails == null) {
        return ResponseEntity.notFound().build();
    }

    LocalDateTime expiryTime =
            (LocalDateTime) fileDetails.get("expiryTime");

    if (LocalDateTime.now().isAfter(expiryTime)) {
        sharedFiles.remove(token);
        return ResponseEntity.notFound().build();
    }

    String storedPassword = (String) fileDetails.get("password");

    if (password == null ||
            !passwordEncoder.matches(password, storedPassword)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String fileName = (String) fileDetails.get("filename");
    Path filePath = uploadDir.resolve(fileName);

    if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
        return ResponseEntity.notFound().build();
    }

    Resource resource = new UrlResource(filePath.toUri());

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"")
            .body(resource);
}


}
