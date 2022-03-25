package de.hybris.contextualattributevaluesbackoffice.dataaccess;

import com.hybris.backoffice.widgets.fulltextsearch.FullTextSearchStrategy;

/**
 * @author florian.mueller07@sap.com
 */
public interface ContextualFulltextSearchStrategy extends FullTextSearchStrategy
{
	boolean isContextual( String typeCode, String fieldName );
}
