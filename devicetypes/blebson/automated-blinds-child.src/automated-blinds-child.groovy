/**
 *  Automated Blinds Child
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
	definition (name: "Automated Blinds Child", namespace: "blebson", author: "Ben Lebson") {
		capability "Battery"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Voltage Measurement"
		capability "Window Shade"
	}
    
    preferences {
    section("Settings:") {
    	input("blindNum", "string", title:"Blind ID Number", description: "Please enter the blind ID number (must match DNI):", required: false, displayDuringSetup: true)
    	input("openAngle", "number", title:"Angle of 'Open' position", description: "Please enter the angle desired for the 'Open' position (1-180):", range: "1..180", required: true, displayDuringSetup: true, defaultValue: 75)
        input("closeRetry", "enum", title:"Numer of retrys for 'Close' button", description: "Please enter the desired number of retrys for the 'Close' command:", displayDuringSetup: true, options: ["1", "2", "3", "4", "5"], defaultValue: "3")
    }
    }

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        controlTile("slider", "device.level", "slider", height: 2, width: 6, inactiveLabel: false, range:"(1..180)") {
    		state "level", action:"windowShade.presetPosition"
		}
        valueTile("battery", "device.battery", inactiveLabel: false, canChangeBackground: true, width: 2, height: 2) {
			state "battery", label:'${currentValue}%', unit:"%",
            backgroundColors:[
				[value: 19, color: "#BC2323"],
				[value: 20, color: "#D04E00"],
				[value: 30, color: "#D04E00"],
				[value: 40, color: "#DAC400"],
				[value: 41, color: "#79b821"]
			]
		}
        valueTile("voltage", "device.voltage", width: 2, height: 2, inactiveLabel: false){
        	state "default", label:'${currentValue}V', unit:"V", defaultState: true
        }
        standardTile("status", "device.status", width: 2, height: 2, inactiveLabel: false, canChangeBackground: true, decoration: "flat"){
        	state "charging", label: "Charging", action: "refresh", icon: "st.Weather.weather14", nextState: "charging"//, backgroundColor: "#f1d801"
            state "charged", label: "Fully Charged", action: "refresh", icon: "st.samsung.da.oven_ic_most_used", nextState: "charged"//, backgroundColor: "#44b621"
            state "notCharging", label: "Not Charging", action: "refresh", icon: "st.Weather.weather4", nextState: "notCharging"//, backgroundColor: "#1e9cbb"
        	state "error", label:'Error', action: "refresh", icon: "st.samsung.da.washer_ic_cancel", nextState: "error"//, backgroundColor: "#bc2323"
        }
        standardTile("open", "command.open", width: 2, height: 2){
        	state "default", label: "Open", action: "windowShade.open", icon: "st.Home.home9", backgroundColor: "#e86d13"
        }
        standardTile("close", "command.close", width: 2, height: 2){
        	state "default", label: "Close", action: "windowShade.close", icon: "st.Home.home9", backgroundColor: "#00a0dc"
        }
        valueTile("blindID", "device.blindID", width: 2, height: 2, inactiveLabel: false, canChangeBackground: true, canChangeIcon: true){
        	state "blindID", label:'ID: ${currentValue}', defaultState: true, icon: "st.Home.home9"
        }
        main "blindID"
        details(["slider", "open", "close", "battery", "blindID", "status", "voltage"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'battery' attribute
	// TODO: handle 'switch' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'voltage' attribute
	// TODO: handle 'windowShade' attribute

}

def installed() {
	String[] deviceID = device.deviceNetworkId.split( '-' )    
    sendEvent(name: "blindID", value: deviceID[1])
    state.blindID = deviceID[1]
}

def updated() {	
	String[] deviceID = device.deviceNetworkId.split( '-' )
    if(blindNum == null){
    	sendEvent(name: "blindID", value: deviceID[1])
        state.blindID = deviceID[1]
    }
    else{
    	sendEvent(name: "blindID", value: blindNum)
        state.blindID = blindNum
    }
}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"
    presetPosition(0)
}

def on() {
	log.debug "Executing 'on'"
    open()
}

def off() {
	log.debug "Executing 'off'"
    close()
}

def setLevel(value) {
	log.debug "Executing 'setLevel'"
    presetPosition(value)
}

def open() {
	log.debug "Executing 'open'"
    presetPosition(openAngle)
}

def close() {
	log.debug "Executing 'close'"
    if (closeRetry.toInteger() == 1){
    	presetPosition(180)
    }
    else if (closeRetry.toInteger() == 2) {
        delayBetween([
        	presetPosition(180),
        	presetPosition(180)
    	], 3000)
    }
    else if (closeRetry.toInteger() == 3) {
    	delayBetween([
        	presetPosition(180),
        	presetPosition(180),
        	presetPosition(180)
    	], 3000)
    }
    else if (closeRetry.toInteger() == 4) {
    	delayBetween([
        	presetPosition(180),
        	presetPosition(180),
            presetPosition(180),
        	presetPosition(180)
    	], 3000)
    }
    else if (closeRetry.toInteger() == 5) {
    	delayBetween([
        	presetPosition(180),
        	presetPosition(180),
            presetPosition(180),
            presetPosition(180),
        	presetPosition(180)
    	], 3000)
    }
    else if (closeRetry == null) {
    	delayBetween([
        	presetPosition(180),
        	presetPosition(180),
        	presetPosition(180)
    	], 3000)
    }
    
}

def presetPosition(value) {
	log.debug "Executing 'presetPosition'"
    if( value != 0 ){
    	sendEvent(name:"level", value:value)
    }
    parent.sendBlindCommand(state.blindID, value)
}
