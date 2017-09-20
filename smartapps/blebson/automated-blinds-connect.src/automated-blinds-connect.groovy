/**
 *  Automated Blinds (Connect)
 *
 *  Copyright 2017 Ben Lebson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Automated Blinds (Connect)",
    namespace: "blebson",
    author: "Ben Lebson",
    description: "Connect app for Automated blinds.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Base Station Settings") {	    
        input("baseIP", "string", title:"Base Station IP Address", description: "Please enter your base station's IP Address", required: true, displayDuringSetup: true)
        input("basePort", "number", title:"Base Station Port", description: "Please enter your base station's Port", required: true, displayDuringSetup: true)   
	}
    section("Blind Settings"){
    	input("blindsCount", "number", title:"Number of Blinds", description: "Please enter the total number of blinds connected to the Base Station", required: true, displayDuringSetup: true)
        input("groupCount", "number", title:"Number of Groups", description: "Please enter the total number of groups defined in the Base Station", required: true, displayDuringSetup: true)        
    }
    section("Hub Settings"){
        input("hubName", "hub", title:"Hub", description: "Please select your Hub", required: true, displayDuringSetup: true)
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    state.baseIP = baseIP
    state.basePort = basePort
    state.blindsCount = blindsCount
    state.groupCount = groupCount
    
    log.debug "Base IP: ${state.baseIP}"
    log.debug "Base Port: ${state.basePort}"
    log.debug "Blinds Count: ${state.blindsCount}"
    log.debug "Group Count: ${state.groupCount}"
    
    def hosthex = convertIPtoHex(baseIP)
    def porthex = convertPortToHex(basePort)
   	def DNI = "$hosthex:$porthex" 
    
    try {
        def base = getChildDevices()
        if (base) {
            base[0].configure()
        }
        else {
        	def childDevice = addChildDevice("blebson", "Automated Blinds Parent", DNI, hubName.id, [name: "BaseStation", label: "Base Station", completedSetup: true])            
        }
    } catch (e) {
    	log.error "Error creating device: ${e}"
    }
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}
// TODO: implement event handlers

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

def getIP() {
	log.debug "getIP()"
	return baseIP
}		

def getPort() {
	log.debug "getPort()"
	return basePort
}	

def getBlinds() {
	log.debug "getBlinds()"
	return blindsCount
}

def getGroup() {
	log.debug "getGroup()"
	return groupCount
}