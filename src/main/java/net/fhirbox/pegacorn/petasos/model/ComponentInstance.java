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

import java.util.Set;

/**
 *
 * @author ACT Health (Mark A. Hunter)
 */
public class ComponentInstance {
    private FDN componentTypeFDN;
    private ComponentInstanceCertificate ciRegistrationCertificate ;
    private Set<FDN> containedComponents;
    private ComponentInstanceStatus ciStatus;
    private Set<WorkUnitProcessor> enabledWUPSet;
    private FDN componentInstanceFDN;
    /**
     * @return the componentInstanceFDN
     */
    public FDN getComponentFDN() {
        return componentInstanceFDN;
    }
    /**
     * @param componentInstanceFDN the componentInstanceFDN to set
     */
    public void setComponentFDN(FDN componentFDN) {
        this.componentInstanceFDN = componentFDN;
    }
    /**
     * @return the componentTypeFDN
     */
    public FDN getComponentTypeFDN() {
        return componentTypeFDN;
    }
    /**
     * @param componentTypeFDN the componentTypeFDN to set
     */
    public void setComponentTypeFDN(FDN componentTypeFDN) {
        this.componentTypeFDN = componentTypeFDN;
    }
    /**
     * @return the ciRegistrationCertificate
     */
    public ComponentInstanceCertificate getCiRegistrationCertificate() {
        return ciRegistrationCertificate;
    }
    /**
     * @param ciRegistrationCertificate the ciRegistrationCertificate to set
     */
    public void setCiRegistrationCertificate(ComponentInstanceCertificate ciRegistrationCertificate) {
        this.ciRegistrationCertificate = ciRegistrationCertificate;
    }
    /**
     * @return the containedComponents
     */
    public Set<FDN> getContainedComponents() {
        return containedComponents;
    }
    /**
     * @param containedComponents the containedComponents to set
     */
    public void setContainedComponents(Set<FDN> containedComponents) {
        this.containedComponents = containedComponents;
    }
    /**
     * @return the ciStatus
     */
    public ComponentInstanceStatus getCiStatus() {
        return ciStatus;
    }
    /**
     * @param ciStatus the ciStatus to set
     */
    public void setCiStatus(ComponentInstanceStatus ciStatus) {
        this.ciStatus = ciStatus;
    }
    /**
     * @return the enabledWUPSet
     */
    public Set<WorkUnitProcessor> getEnabledWUPSet() {
        return enabledWUPSet;
    }
    /**
     * @param enabledWUPSet the enabledWUPSet to set
     */
    public void setEnabledWUPSet(Set<WorkUnitProcessor> enabledWUPSet) {
        this.enabledWUPSet = enabledWUPSet;
    }

}
