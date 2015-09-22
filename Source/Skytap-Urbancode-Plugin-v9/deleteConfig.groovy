import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import groovyx.net.http.ContentType
import com.urbancode.air.AirPluginTool

def apTool = new AirPluginTool(this.args[0], this.args[1])
props = apTool.getStepProperties()
def configID = props['configID']
def username = props['username']
def password = props['password']

def unencodedAuthString = username + ":" + password
def bytes = unencodedAuthString.bytes
encodedAuthString = bytes.encodeBase64().toString()

println "Delete Environment Command Info:"
println "	Environment ID: " + configID
println "Done"

def skytapRESTClient = new RESTClient('https://cloud.skytap.com/')
skytapRESTClient.defaultRequestHeaders.'Authorization: Basic' = encodedAuthString
// skytapRESTClient.defaultRequestHeaders.'Accept' = "application/json"
skytapRESTClient.defaultRequestHeaders.'Content-Type' = "application/json"

def locked = 1

while (locked == 1) {
	try {
		locked = 0
		response = skytapRESTClient.delete(path: "configurations/" + configID,
			requestContentType: ContentType.JSON)
	} catch (HttpResponseException ex) {
		if (ex.statusCode == 423) {
			println "Environment " + configID + " locked. Retrying..."
			locked = 1
			sleep(5000)
                } else if (ex.statusCode == 500) {
                        println "Environment " + configID + " unexpected system error. Retrying..."
                        locked = 1
                        sleep(5000)
		} else if (ex.statusCode == 404) {
			System.err.println ex.statusCode + " - Not Found: " + "https://cloud.skytap.com/configurations/" + configID
			System.exit(1)
		} else {
			System.err.println "Unexpected Error: " + ex.statusCode + " - " + ex.getMessage()
			System.exit(1)
		}
	}
}

println "Environment " + configID + " deleted"