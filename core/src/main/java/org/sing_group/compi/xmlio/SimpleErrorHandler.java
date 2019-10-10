/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
