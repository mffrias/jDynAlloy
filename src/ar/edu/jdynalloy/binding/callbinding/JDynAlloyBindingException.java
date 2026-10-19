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
package ar.edu.jdynalloy.binding.callbinding;


/**
 * An unchecked exception for a call that cannot be bound. {@link CallBindingResolver} throws it,
 * and collects its messages, only when its {@code stopOnFirstBindingError} is false; that field
 * is private, true and never changed, so this exception is never thrown, and binding errors are
 * {@code JDynAlloySemanticException}s instead.
 */
public class JDynAlloyBindingException extends RuntimeException {

	public JDynAlloyBindingException() {
	}

	public JDynAlloyBindingException(String message) {
		super(message);
	}

	public JDynAlloyBindingException(Throwable cause) {
		super(cause);
	}

	public JDynAlloyBindingException(String message, Throwable cause) {
		super(message, cause);
	}

}
