package de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch.renderer.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Popup;

import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.FieldQueryFilter;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.FullTextSearchFilter;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.impl.DefaultFieldQueryPopupRenderer;
import com.hybris.cockpitng.config.fulltextsearch.jaxb.FulltextSearch;
import com.hybris.cockpitng.core.model.ValueObserver;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.common.ProxyRenderer;

public class ContextualFieldQueryPopupRenderer extends DefaultFieldQueryPopupRenderer
{
	private static final String	SCLASS_FIELDQUERY_POPUP_FILTER				= "yw-fulltextsearch-fieldquery-popup-filter";
	private static final String	SCLASS_FIELDQUERY_POPUP_FILTER_DISABLED	= "yw-fulltextsearch-fieldquery-popup-filter-disabled";
	private static final String	SCLASS_FIELDQUERY_POPUP_FILTER_APPLIED		= "yw-fulltextsearch-fieldquery-popup-filter-applied";

	protected void addNewFilter( final Popup parent, final WidgetInstanceManager widgetInstanceManager, final String filterId,
			final FullTextSearchFilter appliedFilter )
	{
		final ProxyRenderer<Popup, FulltextSearch, AdvancedSearchData> proxyRenderer = new ProxyRenderer( this, parent, getFulltextSearchConfig( parent ),
				getAdvancedSearchData( parent ) );
		final Div filterContainer = createContextualFilterContainer( appliedFilter );
		getFieldQueryPopupBody( parent ).appendChild( filterContainer );
		final Div filterButtonsContainer = createFilterButtonsContainer();
		appendRemoveFilterButton( parent, filterButtonsContainer, filterContainer, filterId );
		final Checkbox checkbox = createCheckboxFilter( parent, appliedFilter, filterContainer, filterId );
		filterButtonsContainer.appendChild( checkbox );
		filterContainer.appendChild( filterButtonsContainer );
		final Div filterFields = createFilterFields();
		filterContainer.appendChild( filterFields );
		final FieldQueryFilter fieldQueryFilter = appliedFilter != null ? buildFieldQueryFilter( proxyRenderer.getData(), filterId, appliedFilter )
				: buildFieldQueryFilter( proxyRenderer.getData(), filterId );
		final ContextualFullTextSearchFilter filter = getContextualFilters( parent ).computeIfAbsent( filterId,
				id -> appliedFilter != null ? new ContextualFullTextSearchFilter( appliedFilter ) : new ContextualFullTextSearchFilter() );
		final ValueObserver changeObserver = new ValueObserver()
		{
			@Override
			public void modelChanged( final String property )
			{
				onFieldQueryChange( property, (ContextualFieldQueryFilter) fieldQueryFilter, filter, checkbox );
			}

			@Override
			public void modelChanged()
			{
				// not implemented
			}
		};
		fieldQueryFilter.addObserver( changeObserver );
		proxyRenderer.render( getFieldRenderer(), filterFields, proxyRenderer.getConfig(), fieldQueryFilter, getDataType( parent ), widgetInstanceManager );
		fireComponentRendered( filterContainer, parent, proxyRenderer.getConfig(), proxyRenderer.getData() );
	}

	private Div createContextualFilterContainer(final FullTextSearchFilter value )
	{
		final Div filterContainer = new Div();
		UITools.modifySClass( filterContainer, SCLASS_FIELDQUERY_POPUP_FILTER, true );
		UITools.modifySClass( filterContainer, SCLASS_FIELDQUERY_POPUP_FILTER_APPLIED, value != null );
		return filterContainer;
	}

	private Map<String, ContextualFullTextSearchFilter> getContextualFilters( final Popup parent )
	{
		Map<String, ContextualFullTextSearchFilter> filters = (Map<String, ContextualFullTextSearchFilter>) parent.getAttribute( ATTRIBUTE_FILTERS );
		if( filters == null )
		{
			filters = new LinkedHashMap<>();
			parent.setAttribute( ATTRIBUTE_FILTERS, filters );
		}
		return filters;
	}

	protected FieldQueryFilter buildFieldQueryFilter( final AdvancedSearchData data, final String filterId )
	{
		return new ContextualFieldQueryFilter( filterId, data, (String) null );
	}

	protected FieldQueryFilter buildFieldQueryFilter( final AdvancedSearchData data, final String filterId, final FullTextSearchFilter filter )
	{
		return new ContextualFieldQueryFilter( filterId, data, filter );
	}

	protected void onFieldQueryChange( final String property, final ContextualFieldQueryFilter fieldQueryFilter, final ContextualFullTextSearchFilter filter,
			final Checkbox checkbox )
	{
		super.onFieldQueryChange( property, fieldQueryFilter, filter, checkbox );
		if( ContextualFieldQueryFilter.PROPERTY_CONTEXT.equals( property ) )
		{
			onFieldQueryContextChange( fieldQueryFilter, filter );
		}
	}

	private void onFieldQueryContextChange( ContextualFieldQueryFilter fieldQueryFilter, ContextualFullTextSearchFilter filter )
	{
		filter.setContext( fieldQueryFilter.getContext() );
	}
}
