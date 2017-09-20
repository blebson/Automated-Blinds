/**
 *  Automated Blinds Group
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
	definition (name: "Automated Blinds Group", namespace: "blebson", author: "Ben Lebson") {
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Window Shade"
	}

preferences {
		input("groupName", "string", title:"Three Letter Group Name", description: "Please enter the three letter group name:", required: true, displayDuringSetup: true)
    	input("openAngle", "number", title:"Angle of 'Open' position", description: "Please enter the angle desired for the 'Open' position (1-180):", range: "1..180", required: true, displayDuringSetup: true, defaultValue: 75)
        input("closeRetry", "enum", title:"Numer of retrys for 'Close' button", description: "Please enter the desired number of retrys for the 'Close' command:", displayDuringSetup: true, options: ["1", "2", "3", "4", "5"], defaultValue: "3")
    }

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        controlTile("slider", "device.level", "slider", height: 2, width: 6, inactiveLabel: false, range:"(1..180)") {
    		state "level", action:"windowShade.presetPosition"
		}
		//valueTile("angle", "device.level", width: 2, height: 2, inactiveLabel: false){
        //	state "default", label:'${currentValue}°', unit:"°"
        //}        
        standardTile("open", "command.open", width: 2, height: 2){
        	state "default", label: "Open", action: "windowShade.open", icon: "st.Home.home9", backgroundColor: "#e86d13"
        }
        standardTile("close", "command.close", width: 2, height: 2){
        	state "default", label: "Close", action: "windowShade.close", icon: "st.Home.home9", backgroundColor: "#00a0dc"
        }
        valueTile("groupID", "device.groupID", width: 2, height: 2, inactiveLabel: false, canChangeBackground: true, canChangeIcon: true){
        	state "groupID", label:'ID: ${currentValue}', defaultState: true, icon: "st.Home.home9"
        }
        main "groupID"
        details(["slider", /*"angle",*/ "open", "close", "groupID"])
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
    
}

def updated() {
    sendEvent(name: "groupID", value: groupName)
}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"
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
    sendEvent(name:"level", value:value)    
    parent.sendBlindCommand(groupName, value)
}