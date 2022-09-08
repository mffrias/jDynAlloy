/*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.edu.jdynalloy.binding.symboltable;


public class FieldDescriptor {
    private String moduleName;
    private String fieldName;
    /**
     * @return the type
     */
    public String getType() {
        return moduleName;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.moduleName = type;
    }
    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }
    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public FieldDescriptor() {
	
    }
    public FieldDescriptor(String type,String fieldName) {
	super();
	this.moduleName = type;
	this.fieldName = fieldName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
	result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
	return result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	FieldDescriptor other = (FieldDescriptor) obj;
	if (fieldName == null) {
	    if (other.fieldName != null)
		return false;
	} else if (!fieldName.equals(other.fieldName))
	    return false;
	if (moduleName == null) {
	    if (other.moduleName != null)
		return false;
	} else if (!moduleName.equals(other.moduleName))
	    return false;
	return true;
    }
    
    @Override
    public String toString() {
    	return moduleName + "." + fieldName;
    }
    
    
}
