import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

import org.openqa.selenium.WebDriver
import org.openqa.selenium.devtools.DevTools
import org.openqa.selenium.devtools.HasDevTools
import org.openqa.selenium.devtools.v143.network.Network
import org.openqa.selenium.devtools.v143.network.model.RequestId
import org.openqa.selenium.remote.Augmenter

import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// 1. Open the Browser
WebUI.openBrowser('')

// 2. Initialize DevTools
WebDriver driver = DriverFactory.getWebDriver()
// "Augment" the wrapped Katalon driver so it can access DevTools
WebDriver augmentedDriver = new Augmenter().augment(driver)
DevTools devTools = ((HasDevTools) augmentedDriver).getDevTools()
devTools.createSession()
//devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))
devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))

// 3. Create an empty bucket to hold the data
AtomicReference<String> savedResponse = new AtomicReference<String>("")

// 4. Turn on the background listener BEFORE doing any UI actions
devTools.addListener(Network.responseReceived(), { responseReceived ->
	String url = responseReceived.getResponse().getUrl()
	
	if (url.contains("pokemon/ditto")) {
		RequestId requestId = responseReceived.getRequestId()
		try {
			def responseBodyObj = devTools.send(Network.getResponseBody(requestId))
			
			// Check if the response is Base64 encoded
			String bodyText = responseBodyObj.getBody()
			if (responseBodyObj.getBase64Encoded()) {
				// If it is encoded, decode it back to plain text JSON
				byte[] decodedBytes = Base64.getDecoder().decode(bodyText)
				bodyText = new String(decodedBytes)
			}
			
			savedResponse.set(bodyText)
			
		} catch (Exception e) {
			println("Error capturing body: " + e.getMessage())
		}
	}
} as Consumer)

// ---------------------------------------------------------
// YOUR UI TEST STEPS START HERE
// ---------------------------------------------------------

// 5. Navigate to the page
WebUI.navigateToUrl('https://pokeapi.co/')
WebUI.delay(2)

// 6. Click the Submit button to trigger the API
println("Clicking the Submit button...")
WebUI.click(findTestObject('Page_PokAPI/button_Submit'))

// 7. Wait 4 seconds to give the API time to return the data
WebUI.delay(4)

// ---------------------------------------------------------
// FETCH AND PRINT THE DATA (AFTER THE CLICK)
// ---------------------------------------------------------

// 8. Pull the data out of the bucket we created at the top
String finalJsonString = savedResponse.get()

if (finalJsonString != "") {
	println("\n=============================================")
	println("SUCCESS! FULL RAW API RESPONSE:")
	
	// This formats the raw JSON string so it is readable
	String prettyJson = JsonOutput.prettyPrint(finalJsonString)
	println(prettyJson)
	
	println("=============================================\n")
	
	
	// 1. Parse the string into a Groovy object
	def parsedJson = new JsonSlurper().parseText(finalJsonString)
	
	// 2. Access the 'weight' key directly
	def pokemonWeight = parsedJson.weight
	
	println("\n=============================================")
	println("FETCHING SPECIFIC DATA:")
	println("The weight of " + parsedJson.name + " is: " + pokemonWeight)
	println("=============================================\n")
	
	
} else {
	println("FAILED: The bucket is empty. The API response was not caught.")
}

// 10. Clean up
WebUI.closeBrowser()