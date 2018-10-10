/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.xmlio;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * Implements an {@link ErrorHandler} for the SAX parsing
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class SimpleErrorHandler implements ErrorHandler {

	/**
	 * Prints a {@link SAXParseException} warning
	 */
	@Override
	public void warning(final SAXParseException exception) throws SAXException {
		exception.printStackTrace();
	}

	/**
	 * Throws a {@link SAXParseException} exception when a fatal error occurs
	 */
	@Override
	public void fatalError(final SAXParseException exception) throws SAXException {
		throw exception;
	}

	/**
	 * Throws a {@link SAXParseException} exception when an error occurs
	 */
	@Override
	public void error(final SAXParseException exception) throws SAXException {
		throw exception;
	}
}
