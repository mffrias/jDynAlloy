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

package ar.edu.jdynalloy.parser;

public class JDynAlloyParsingException extends RuntimeException {

    private static final long serialVersionUID = -6194214972653008786L;

    String fileOrResourceName = null;

    public JDynAlloyParsingException(String fileOrResourceName) {
	super();
	this.fileOrResourceName = fileOrResourceName;
    }

    public JDynAlloyParsingException(String arg0, Throwable arg1,String fileOrResourceName) {
	super(arg0, arg1);
	this.fileOrResourceName = fileOrResourceName;	
    }

    public JDynAlloyParsingException(String arg0,String fileOrResourceName) {
	super(arg0);
	this.fileOrResourceName = fileOrResourceName;	
    }

    public JDynAlloyParsingException(Throwable arg0,String fileOrResourceName) {
	super(arg0);
	this.fileOrResourceName = fileOrResourceName;	
    }

    @Override
    public String getMessage() {        
        return super.getMessage() + " . in file or resource " + fileOrResourceName;
    }
}
