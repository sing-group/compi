package es.uvigo.esei.compi.xmlio;

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