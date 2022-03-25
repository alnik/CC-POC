package com.hybris.cockpitng.widgets.compare.renderer;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.Collection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.components.table.TableCell;
import com.hybris.cockpitng.config.compareview.jaxb.Attribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.compare.model.ComparisonState;
import com.hybris.cockpitng.widgets.compare.model.PartialRendererData;

public class ContextualAttributeCompareViewAttributeRenderer extends DefaultCompareViewAttributeRenderer
{
	@Override
	protected void renderAttributeValueContents( final HtmlBasedComponent parent, final Attribute attribute, final PartialRendererData<Collection> data,
			final Object item, final DataType dataType, final WidgetInstanceManager widgetInstanceManager )
	{
		if( attribute instanceof ContextualAttributesCompareViewSectionRenderer.ContextualAttribute )
		{
			final Object contextualAttributeValueItem = getContextualAttributeForContext(
					((ContextualAttributesCompareViewSectionRenderer.ContextualAttribute) attribute).getContext(), item );
			super.renderAttributeValueContents( parent, attribute, data, contextualAttributeValueItem, dataType, widgetInstanceManager );
		}
		else
		{
			super.renderAttributeValueContents( parent, attribute, data, item, dataType, widgetInstanceManager );
		}
	}

	@Override
	protected void attributeValueRendered( TableCell attributeValue, Attribute configuration, PartialRendererData<Collection> data, Object item,
			DataType dataType, WidgetInstanceManager widgetInstanceManager )
	{
		// add custom styling class
		UITools.addSClass( attributeValue, "yw-compareview-attribute-value yw-compareview-context-attribute-value" );
		this.updateAttributeValueDifferentMark( configuration, item, data, attributeValue );
		boolean inProgress = ComparisonState.Status.FINISHED != data.getComparisonState().getStatus()
				&& !data.getComparisonState().getComparedObjects().contains( item );
		if( inProgress )
		{
			DefaultCompareViewLayout.markAsDuringCalculation( attributeValue );
		}
		else
		{
			DefaultCompareViewLayout.markAsCalculated( attributeValue );
		}
		Object cachedValue = this.getAttributeValueCache( configuration, data, item, dataType, widgetInstanceManager );
		attributeValue.setAttribute( "attribute-value-cache", cachedValue );
		attributeValue.setSticky( this.getItemComparisonFacade().isSameItem( data.getComparisonState().getReference(), item ) );
	}

	@Override
	protected Component createAttributeNameLabel( Component parent, Attribute configuration, PartialRendererData<Collection> data, DataType dataType,
			WidgetInstanceManager widgetInstanceManager )
	{
		String attributeName = this.getAttributeName( configuration, data, dataType, widgetInstanceManager );
		Label attributeNameLabel = new Label( attributeName );
		// add custom styling class
		UITools.addSClass( attributeNameLabel, "yw-compareview-attribute-name-label yw-compareview-context-attribute-name-label" );
		return attributeNameLabel;
	}

	private Object getContextualAttributeForContext( final ContextualAttributesContextModel attributeContext, final Object item )
	{
		final Collection<ContextualAttributeValueModel> contextualAttributeValues = ((ProductModel) item).getContextualAttributeValues();
		for( ContextualAttributeValueModel contextualAttributeValue: contextualAttributeValues )
		{
			if( contextualAttributeValue.getContext().equals( attributeContext ) )
			{
				return contextualAttributeValue;
			}
		}
		return ContextualAttributeValueModel._TYPECODE;
	}
}
