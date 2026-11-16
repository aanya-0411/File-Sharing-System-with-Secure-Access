
// ===============================
// FileShare - Main JavaScript
// ===============================

// HTML Elements
const authSection = document.getElementById("authSection");
const appSection = document.getElementById("appSection");

const authTitle = document.getElementById("authTitle");
const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");

const loginUsername = document.getElementById("loginUsername");
const loginPassword = document.getElementById("loginPassword");

const registerUsername = document.getElementById("registerUsername");
const registerPassword = document.getElementById("registerPassword");

const loginBtn = document.getElementById("loginBtn");
const registerBtn = document.getElementById("registerBtn");

const showRegister = document.getElementById("showRegister");
const showLogin = document.getElementById("showLogin");

const logoutBtn = document.getElementById("logoutBtn");
const userWelcome = document.getElementById("userWelcome");

const fileInput = document.getElementById("fileInput");
const fileList = document.getElementById("fileList");
const clearFilesBtn = document.getElementById("clearFilesBtn");


// Saved filenames
let savedFiles = JSON.parse(localStorage.getItem("savedFiles")) || [];


// ===============================
// Helper Functions
// ===============================

function showMessage(message) {
    alert(message);
}

function saveFiles() {
    localStorage.setItem("savedFiles", JSON.stringify(savedFiles));
}

function showLoginPage() {
    authSection.style.display = "block";
    appSection.style.display = "none";

    loginForm.style.display = "block";
    registerForm.style.display = "none";

    authTitle.textContent = "Login to FileShare";
}

function showApp(username) {
    authSection.style.display = "none";
    appSection.style.display = "block";

    userWelcome.textContent = "Welcome, " + username + "!";
    renderFiles();
}


// ===============================
// Login / Register Form Switching
// ===============================

showRegister.addEventListener("click", function (event) {
    event.preventDefault();

    loginForm.style.display = "none";
    registerForm.style.display = "block";

    authTitle.textContent = "Create Your Account";
});

showLogin.addEventListener("click", function (event) {
    event.preventDefault();

    registerForm.style.display = "none";
    loginForm.style.display = "block";

    authTitle.textContent = "Login to FileShare";
});


// ===============================
// Register
// ===============================

registerBtn.addEventListener("click", async function () {

    const username = registerUsername.value.trim();
    const password = registerPassword.value;

    if (!username || !password) {
        showMessage("Please enter username and password.");
        return;
    }

    if (password.length < 6) {
        showMessage("Password must be at least 6 characters.");
        return;
    }

    try {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Registration failed.");
        }

        showMessage("Registration successful! Please login.");

        registerUsername.value = "";
        registerPassword.value = "";

        showLoginPage();

    } catch (error) {
        showMessage(error.message);
    }
});


// ===============================
// Login
// ===============================

loginBtn.addEventListener("click", async function () {

    const username = loginUsername.value.trim();
    const password = loginPassword.value;

    if (!username || !password) {
        showMessage("Please enter username and password.");
        return;
    }

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Login failed.");
        }

        loginUsername.value = "";
        loginPassword.value = "";

        showApp(data.username);

    } catch (error) {
        showMessage(error.message);
    }
});


// ===============================
// Check Existing Login Session
// ===============================

async function checkLogin() {

    try {
        const response = await fetch("/api/auth/me");

        if (response.ok) {
            const data = await response.json();
            showApp(data.username);
        } else {
            showLoginPage();
        }

    } catch (error) {
        showLoginPage();
    }
}


// ===============================
// Logout
// ===============================

logoutBtn.addEventListener("click", async function () {

    try {
        const response = await fetch("/api/auth/logout", {
            method: "POST"
        });

        if (!response.ok) {
            showMessage("Logout failed.");
            return;
        }

        showLoginPage();
        showMessage("Logged out successfully!");

    } catch (error) {
        showMessage("Unable to logout. Please try again.");
    }
});


// ===============================
// Render My Files
// ===============================

function renderFiles() {

    fileList.innerHTML = "";

    if (savedFiles.length === 0) {
        const emptyMessage = document.createElement("p");
        emptyMessage.className = "empty-message";
        emptyMessage.textContent = "No files uploaded yet.";

        fileList.appendChild(emptyMessage);
        return;
    }

    savedFiles.forEach(function (filename) {

        const fileItem = document.createElement("div");
        fileItem.className = "file-item";

        const name = document.createElement("span");
        name.textContent = "📄 " + filename;

        const downloadBtn = document.createElement("button");
        downloadBtn.textContent = "Download";
        downloadBtn.className = "download-btn";

        downloadBtn.addEventListener("click", function () {
            downloadFile(filename);
        });

        const shareBtn = document.createElement("button");
        shareBtn.textContent = "Share";
        shareBtn.className = "download-btn";

        shareBtn.addEventListener("click", function () {
            shareFile(filename);
        });

        fileItem.appendChild(name);
        fileItem.appendChild(downloadBtn);
        fileItem.appendChild(shareBtn);

        fileList.appendChild(fileItem);
    });
}


// ===============================
// Upload Files
// ===============================

fileInput.addEventListener("change", async function () {

    const files = Array.from(fileInput.files);

    if (files.length === 0) {
        return;
    }

    for (const file of files) {

        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch("/api/upload", {
                method: "POST",
                body: formData
            });

            if (response.status === 401) {
                showMessage("Please login again.");
                showLoginPage();
                return;
            }

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || "Upload failed.");
            }

            if (!savedFiles.includes(file.name)) {
                savedFiles.push(file.name);
            }

            saveFiles();

        } catch (error) {
            showMessage(
                "Upload failed for " + file.name + ": " + error.message
            );
        }
    }

    renderFiles();
    fileInput.value = "";

    showMessage("File upload process completed!");
});


// ===============================
// Download Files
// ===============================

function downloadFile(filename) {

    const url = "/api/download?filename=" + encodeURIComponent(filename);

    window.location.href = url;
}


// ===============================
// Password Protected Sharing
// ===============================


async function shareFile(filename) {

    const password = prompt(
        "Set a password for this share link (minimum 6 characters):"
    );

    if (password === null) return;

    if (password.length < 6) {
        showMessage("Password must be at least 6 characters.");
        return;
    }

    const formData = new URLSearchParams();
    formData.append("filename", filename);
    formData.append("password", password);

    try {
        const response = await fetch("/api/share", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: formData
        });

        const responseText = await response.text();

        if (response.status === 401) {
            showMessage("Please login again.");
            showLoginPage();
            return;
        }

        if (!response.ok) {
            throw new Error(responseText || "Unable to create share link.");
        }

        const shareUrl = responseText.trim();

        if (!shareUrl.startsWith("http")) {
            throw new Error("Invalid share link received.");
        }

        try {
            await navigator.clipboard.writeText(shareUrl);
            showMessage(
                "Share link copied successfully! Send the password separately."
            );
        } catch (error) {
            window.prompt("Copy your share link:", shareUrl);
        }

    } catch (error) {
        showMessage("Sharing failed: " + error.message);
    }
}



// ===============================
// Clear My Files List
// ===============================

clearFilesBtn.addEventListener("click", function () {

    if (savedFiles.length === 0) {
        showMessage("Your file list is already empty.");
        return;
    }

    const confirmClear = confirm(
        "Are you sure you want to clear your My Files list?"
    );

    if (confirmClear) {
        savedFiles = [];
        saveFiles();
        renderFiles();

        showMessage("My Files list cleared successfully!");
    }
});


// ===============================
// Start Application
// ===============================

checkLogin();
