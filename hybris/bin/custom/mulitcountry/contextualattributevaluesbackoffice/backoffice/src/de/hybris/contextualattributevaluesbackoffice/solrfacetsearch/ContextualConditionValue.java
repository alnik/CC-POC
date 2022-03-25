package de.hybris.contextualattributevaluesbackoffice.solrfacetsearch;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualConditionValue implements Serializable
{
	private List<ContextualAttributesContextModel>	context;
	private Locale												locale	= null;
	private Object												value;

	public List<ContextualAttributesContextModel> getContext()
	{
		return context;
	}

	public void setContext( List<ContextualAttributesContextModel> context )
	{
		this.context = context;
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale( Locale locale )
	{
		this.locale = locale;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue( Object value )
	{
		this.value = value;
	}
}
