package de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch.renderer.impl;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.List;

import com.hybris.backoffice.widgets.fulltextsearch.renderer.FullTextSearchFilter;

public class ContextualFullTextSearchFilter extends FullTextSearchFilter
{
	private List<ContextualAttributesContextModel> context;

	public ContextualFullTextSearchFilter( FullTextSearchFilter appliedFilter )
	{
		super( appliedFilter );
		if( appliedFilter instanceof ContextualFullTextSearchFilter )
		{
			this.context = ((ContextualFullTextSearchFilter) appliedFilter).getContext();
		}
	}

	public ContextualFullTextSearchFilter()
	{
		super();
	}

	public List<ContextualAttributesContextModel> getContext()
	{
		return context;
	}

	public void setContext( List<ContextualAttributesContextModel> context )
	{
		this.context = context;
		changed( ContextualFieldQueryFilter.PROPERTY_CONTEXT );
	}
}
