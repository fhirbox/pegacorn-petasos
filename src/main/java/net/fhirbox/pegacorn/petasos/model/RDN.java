/* 
 * The MIT License
 *
 * Copyright 2016 Mark A. Hunter.
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
 * @author markh
 */
public class RDN 
{
    private String nameType;
    private String nameValue;

    public String BAD_NAME_VALUE = "ERROR_BAD_NAME_VALUE";
    public String BAD_NAME_TYPE = "ERROR_BAD_NAME_TYPE";
    
    public RDN(String nmType, String value) {
        this.nameType = nmType;
        this.nameValue = value;
    }
    
    public RDN(String qualifiedRDNName){ // String of type "type=value"
        if( qualifiedRDNName == null){
            this.nameType = this.BAD_NAME_TYPE;
            this.nameValue = this.BAD_NAME_VALUE;
            return;
        }
        if( qualifiedRDNName.isEmpty()){
            this.nameType = this.BAD_NAME_TYPE;;
            this.nameValue = this.BAD_NAME_VALUE;          
            return;
        }
        String[] namePair = qualifiedRDNName.split("=");
        if( namePair.length > 2){
            this.nameType = this.BAD_NAME_TYPE;;
            this.nameValue = this.BAD_NAME_VALUE;          
            return;            
        }
        this.nameType = namePair[0];
        this.nameValue = namePair[1];
    }
    
    
    public String getTypeName() {
        return nameType;
    }

    public void setTypeName(String name) {
        this.nameType = name;
    }

    public String getTypeValue() {
        return nameValue;
    }

    public void setTypeValue(String value) {
        this.nameValue = value;
    }
    
    public String toString()
    {
        return(nameType + "=" + nameValue);
    }
    
    public String toRDNDescription()
    {
        return("RDN:" + nameType + "=" + nameValue);
    }
}
