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

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author ACT Health (Mark A. Hunter)
 */
public class ComponentInstance {
    private ArrayList<FDN> supportedFunctionSet;
    private ArrayList<ComponentInstance> containedComponents;
    private ArrayList<WorkUnitProcessor> enabledWUPSet;
    private FDN componentFDN;
    private ComponentInstanceStatus componentStatus; 
    

    /**
     * @return the componentInstanceFDN
     */
    public FDN getComponentFDN() {
        return componentFDN;
    }
    /**
     * @param componentInstanceFDN the componentInstanceFDN to set
     */
    public void setComponentFDN(FDN componentFDN) {
        this.componentFDN = componentFDN;
    }
    /**
     * @return the supportedFunctionSet
     */
    public Collection<FDN> getSupportedFunctionSet() {
        return supportedFunctionSet;
    }
    /**
     * @param supportedFunctionSet the supportedFunctionSet to set
     */
    public void setSupportedFunctionSet(Collection<FDN> newSupportedFunctionSet) {
        this.supportedFunctionSet.clear();
        this.supportedFunctionSet.addAll(newSupportedFunctionSet);
    }
    /**
     * @return the containedComponents
     */
    public Collection<ComponentInstance> getContainedComponents() {
        return containedComponents;
    }
    /**
     * @param containedComponents the containedComponents to set
     */
    public void setContainedComponents(Collection<ComponentInstance> containedComponents) {
        this.containedComponents.clear();
        this.containedComponents.addAll(containedComponents);
    }
    /**
     * @return the componentStatus
     */
    public ComponentInstanceStatus getComponentStatus() {
        return componentStatus;
    }
    /**
     * @param componentStatus the componentStatus to set
     */
    public void setComponentStatus(ComponentInstanceStatus ciStatus) {
        this.componentStatus = ciStatus;
    }
    /**
     * @return the enabledWUPSet
     */
    public Collection<WorkUnitProcessor> getEnabledWUPSet() {
        return enabledWUPSet;
    }
    /**
     * @param enabledWUPSet the enabledWUPSet to set
     */
    public void setEnabledWUPSet(Collection<WorkUnitProcessor> enabledWUPSet) {
        this.enabledWUPSet.clear();
        this.enabledWUPSet.addAll(enabledWUPSet);
    }

}
