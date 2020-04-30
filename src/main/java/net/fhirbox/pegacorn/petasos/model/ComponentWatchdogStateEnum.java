/*
 * The MIT License
 *
 * Copyright 2020 ACT Health (Mark A. Hunter).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.fhirbox.pegacorn.petasos.model;

/**
 *
 * @author ACT Health (Mark A. Hunter)
 */
public enum ComponentWatchdogStateEnum {
    COMPONENT_STATUS_ACTIVE("pegacorn.component.watchdog.state.active"),
    COMPONENT_STATUS_IDLE("pegacorn.component.watchdog.state.idle"),
    COMPONENT_STATUS_UNRESPONSIVE("pegacorn.component.watchdog.state.unresponsive"),
    COMPONENT_STATUS_FAILED("pegacorn.component.watchdog.state.unresponsive");
    
    private String componentWatchdogState;
    
    private ComponentWatchdogStateEnum(String componentWatchdogState ){
        this.componentWatchdogState = componentWatchdogState;
    }
    
    public String getComponentWatchdogState(){
        return(this.componentWatchdogState);
    }
    
}
