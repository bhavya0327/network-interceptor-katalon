# network-interceptor-katalon
A custom Katalon Studio implementation using Selenium 4 Chrome DevTools Protocol (CDP) to intercept, decode, and extract data from background API network responses during UI automation.

# Katalon Network Interceptor 🚀

This project demonstrates how to capture background API (XHR/Fetch) responses directly within **Katalon Studio** tests. By leveraging the **Selenium 4 Chrome DevTools Protocol (CDP)**, we can listen to network traffic, decode Base64 payloads, and validate API data that isn't visible on the UI.

## 📋 Features
* **Real-time Interception:** Capture API responses the moment they are triggered by UI actions.
* **Automatic Decoding:** Handles Base64 encoded bodies commonly returned by modern web apps.
* **JSON Parsing:** Extracts specific attributes from the full response for test assertions.
* **Asynchronous Handling:** Uses `AtomicReference` to safely pass data from background listeners to the main test thread.

## 🛠️ Prerequisites
* **Katalon Studio** (v8.x or higher)
* **Chrome Browser**
* **Selenium 4** (Bundled with modern Katalon versions)

## 🚀 How It Works
The core logic "sets a trap" (a listener) before the UI action occurs. Even if the API call happens in milliseconds, the listener captures the data into a thread-safe "bucket" which can be accessed after the UI action is complete.

### Core Implementation
The script uses `Augmenter` to bypass Katalon's driver wrappers:
```groovy
WebDriver augmentedDriver = new Augmenter().augment(driver)
DevTools devTools = ((HasDevTools) augmentedDriver).getDevTools()
