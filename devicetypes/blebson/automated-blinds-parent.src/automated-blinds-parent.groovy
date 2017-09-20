/**
 *  Automated Blinds Parent
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
metadata {
	definition (name: "Automated Blinds Parent", namespace: "blebson", author: "Ben Lebson") {
		capability "Battery"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Voltage Measurement"
		capability "Window Shade"
	}    
	
	simulator {
		// TODO: define status and reply messages here
        
	}

	tiles (scale: 2){
        valueTile("baseIP", "device.baseIP", width: 4, height: 2, inactiveLabel: false){
        	state "default", label: 'IP: ${currentValue}', delaultState: true
        }
        valueTile("basePort", "device.basePort", width: 2, height: 2, inactiveLabel: false){
        	state "default", label: 'Port: ${currentValue}', delaultState: true
        }
        valueTile("blindsCount", "device.blindsCount", width: 2, height: 2, inactiveLabel: false){
        	state "default", label: 'Blinds: ${currentValue}', delaultState: true
        }
        valueTile("groupCount", "device.groupCount", width: 2, height: 2, inactiveLabel: false){
        	state "default", label: 'Groups: ${currentValue}', delaultState: true
        }
        
        standardTile("refresh", "command.refresh", width: 2, height: 2, inactiveLabel: false) {
        	state "default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"        
    	}
        main "refresh"
        details(["baseIP", "basePort", "blindsCount", "groupCount", "refresh"])
	}
}

def installed() {
	configure()
	createChildDevices()
}

def updated() {
	configure()
}

private void createChildDevices() {
	log.debug "createChildDevices()"
	int blindsCount = state.blindsCount
    int groupCount = state.groupCount
    log.debug "${state.blindsCount}"
    log.debug "${state.groupCount}"
	int i = 1
	while (i <= blindsCount) {
    	log.debug "Adding Blinds"
    	addChildDevice("Automated Blinds Child", "${device.deviceNetworkId}-${i}", location.hubs[0].id, [isComponent: false, completedSetup: true, label: "Blind ${i}", componentName: "blind${i}", componentLabel: "Blind ${i}"])
        i++
    }
    i = 1
    while (i <= groupCount) {
    	log.debug "Adding Groups"
    	addChildDevice("Automated Blinds Group", "${device.deviceNetworkId}-G${i}", location.hubs[0].id, [isComponent: false, completedSetup: true, label: "Group ${i}", componentName: "group${i}", componentLabel: "Group ${i}"])
        i++
    }
}

def configure(){

	state.baseIP = parent.getIP()
    state.basePort = parent.getPort()
    state.blindsCount = parent.getBlinds()
    state.groupCount = parent.getGroup()
    
    sendEvent(name: 'baseIP', value: state.baseIP)
    sendEvent(name: 'basePort', value: state.basePort)
    sendEvent(name: 'blindsCount', value: state.blindsCount)
    sendEvent(name: 'groupCount', value: state.groupCount)
}


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
    def map = [:]
	def retResult = []
	def descMap = parseDescriptionAsMap(description)
    def msg = parseLanMessage(description)    
    log.debug "data ${msg.data}"
    def body = new String(descMap["body"].decodeBase64())
    log.debug "Body: ${body}"
    
    if(msg.body != null && msg.body.contains("Success."))
        {
        	//log.debug msg.body
            String[] parseBody = msg.body.split( '<html>' )
        	String[] lines = parseBody[1].split( '<br>' )        	
            String[] parseID = lines[0].split( ': ' )
            //log.debug "ID: ${parseID[1]}"
            String[] parseGroup = lines[1].split( ': ' )
            //log.debug "Group: ${parseGroup[1]}"
            String[] parseLevel = lines[2].split( ': ' )
            //log.debug "Level: ${parseLevel[1]}"
            String[] parseBattery = lines[3].split( ': ' )
            //log.debug "Battery: ${parseBattery[1]}"
            String[] parseVoltage = lines[4].split( ': ' )
            //log.debug "Voltage: ${parseVoltage[1]}"
            String[] parseCharger = lines[5].split( ': ' )
            //log.debug "Charger: ${parseCharger[1]}"
            //Blind ID: MBR<br>Group: MBR<br>Level: 180<br>Battery: 32<br>Voltage: 5.65<br>Charger: 
            
            def children = getChildDevices()

	        children.each { child->
        	String[] childID = child.deviceNetworkId.split( '-' )            
            if(childID[1] == parseID[1]) {
            	if ( parseBattery != null){
            		child.sendEvent(name: "battery", value: parseBattery[1])
                }
                else{
                	log.warn "Warning: Returned Battery value is Null."
                }
                if ( parseVoltage != null){
                	child.sendEvent(name: "voltage", value: parseVoltage[1])
                }
                else{
                	log.warn "Warning: Returned Voltage value is Null."
                }
                if ( parseCharger != null ){
                	if(parseCharger[1] == "C"){
                    	//log.debug "Charging"
                		child.sendEvent(name: "status", value: "charging")
                    }
                    else if(parseCharger[1] == "O"){
                    	//log.debug "FullyCharged"
                		child.sendEvent(name: "status", value: "charged")
                    }
                    else if(parseCharger[1] == "N"){
                    	//log.debug "Not Charging"
                		child.sendEvent(name: "status", value: "notCharging")
                    }
                    else if(parseCharger[1] == "E"){
                    	//log.debug "Error"
                		child.sendEvent(name: "status", value: "error")
                    }
                }
                else{
                	log.warn "Warning: Returned Status value is Null."
                }
            }
            else if(parseID[1].contains(childID[1])){
            	child.sendEvent(name: "level", value: "${parseLevel[1]}")
            }
            else if(parseID[1] == "ALL"){
            	child.sendEvent(name: "level", value: "${parseLevel[1]}")
            }
        }    
    }
	// TODO: handle 'battery' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'voltage' attribute
	// TODO: handle 'windowShade' attribute

}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"
    def children = getChildDevices()
    children.each { child->
    	child.updated()
    }
    
	// TODO: handle 'refresh' command
}

def sendBlindCommand(String address, int value) {
	log.debug "sendBlindCommand()"
	def headers = [:] 
    headers.put("HOST", "$state.baseIP:$state.basePort")

	 def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: "/Blind/ID=${address}/level=${value}",
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}