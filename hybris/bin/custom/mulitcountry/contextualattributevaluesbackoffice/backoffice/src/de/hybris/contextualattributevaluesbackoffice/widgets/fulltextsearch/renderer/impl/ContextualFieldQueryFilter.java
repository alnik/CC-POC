package de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch.renderer.impl;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.List;

import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.FieldQueryFilter;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.FullTextSearchFilter;

public class ContextualFieldQueryFilter extends FieldQueryFilter
{
	public static final String								PROPERTY_CONTEXT	= "context";
	private List<ContextualAttributesContextModel>	context;

	public ContextualFieldQueryFilter( String filterId, AdvancedSearchData data, String s )
	{
		super( filterId, data, s );
	}

	public ContextualFieldQueryFilter( String filterId, AdvancedSearchData data, FullTextSearchFilter filter )
	{
		super( filterId, data, filter );
		if( filter instanceof ContextualFullTextSearchFilter )
		{
			this.context = ((ContextualFullTextSearchFilter) filter).getContext();
		}
	}

	public List<ContextualAttributesContextModel> getContext()
	{
		return context;
	}

	public void setContext( List<ContextualAttributesContextModel> context )
	{
		this.context = context;
		changed( PROPERTY_CONTEXT );
	}
}
