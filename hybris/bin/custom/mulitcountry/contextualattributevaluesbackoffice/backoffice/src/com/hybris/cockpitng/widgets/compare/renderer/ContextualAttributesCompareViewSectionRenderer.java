package com.hybris.cockpitng.widgets.compare.renderer;

import de.hybris.platform.contextualattributevalues.daos.ContextualAttributesDao;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.components.table.TableCell;
import com.hybris.cockpitng.components.table.TableRow;
import com.hybris.cockpitng.components.table.TableRowsGroup;
import com.hybris.cockpitng.components.table.iterator.TableComponentIterator;
import com.hybris.cockpitng.config.compareview.jaxb.Attribute;
import com.hybris.cockpitng.config.compareview.jaxb.Section;
import com.hybris.cockpitng.core.util.ObjectValuePath;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.ProxyRenderer;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import com.hybris.cockpitng.widgets.compare.model.LocalizedAttributeInfo;
import com.hybris.cockpitng.widgets.compare.model.PartialRendererData;

public class ContextualAttributesCompareViewSectionRenderer extends DefaultCompareViewSectionRenderer
{
	private WidgetComponentRenderer<TableRow, Attribute, PartialRendererData<Collection>>	attributeRenderer;
	private ContextualAttributesDao																			contextualAttributesDao;

	@Override
	protected void renderSection( TableRowsGroup parent, TableRow headerRow, Section configuration, PartialRendererData<Collection> data, DataType dataType,
			WidgetInstanceManager widgetInstanceManager )
	{
		final TableComponentIterator<TableRowsGroup> rowsGroups = parent.groupsIterator();
		final TableComponentIterator<TableRow> rows = parent.rowsIterator();
		// loop through available contexts, set configuration parameter for each attribute & retrieve appropriate contextual values (in
		// ContextualAttributeCompareViewAttributeRenderer)
		final List<ContextualAttributesContextModel> allAvailableContexts = getContextualAttributesDao().findAllAvailableContexts();
		for( ContextualAttributesContextModel contextModel: allAvailableContexts )
		{
			renderContextualHeader( parent, headerRow, configuration, data, dataType, widgetInstanceManager, rowsGroups, rows, contextModel );
			renderContextualAttributes( parent, headerRow, configuration, data, dataType, widgetInstanceManager, rowsGroups, rows, contextModel );
		}
		rowsGroups.removeRemaining();
		rows.removeRemaining();
	}

	private void renderContextualHeader( final TableRowsGroup parent, final TableRow headerRow, final Section configuration,
			final PartialRendererData<Collection> data, final DataType dataType, final WidgetInstanceManager widgetInstanceManager,
			final TableComponentIterator<TableRowsGroup> rowsGroups, final TableComponentIterator<TableRow> rows,
			final ContextualAttributesContextModel contextModel )
	{
		TableRow row = rows.request();
		row.setSclass( "y-tablerow-header y-tablerow-header-context" );
		TableComponentIterator<TableCell> cellIterator = row.cellsIterator();
		TableCell firstCell = cellIterator.request();
		firstCell.setSticky( true );
		firstCell.setClass( "yw-compareview-attribute-name" );
		firstCell.appendChild( buildContextualLabelContainer( contextModel ) );
		fireComponentRendered( firstCell, parent, configuration, data );
		Iterator dataIterator = data.getData().iterator();
		while( dataIterator.hasNext() )
		{
			dataIterator.next();
			TableCell cell = cellIterator.request();
			if( dataIterator.hasNext() )
			{
				cell.setSticky( true );
			}
			cell.setClass( "yw-compareview-attribute-value" );
			cell.appendChild( buildContextualLabelContainer( contextModel ) );
			fireComponentRendered( cell, parent, configuration, data );
		}
		parent.appendChild( row );
		this.fireComponentRendered( row, parent, configuration, data );
	}

	private Component buildContextualLabelContainer( final ContextualAttributesContextModel contextModel )
	{
		final Div outerDiv = new Div();
		final Div innerDiv = new Div();
		outerDiv.setSclass( "yw-compareview-cell-content" );
		final Label label = new Label( contextModel.getCode() );
		outerDiv.appendChild( innerDiv );
		innerDiv.appendChild( label );
		return outerDiv;
	}

	private void renderContextualAttributes( TableRowsGroup parent, TableRow headerRow, Section configuration, PartialRendererData<Collection> data,
			DataType dataType, WidgetInstanceManager widgetInstanceManager, TableComponentIterator<TableRowsGroup> rowsGroups,
			TableComponentIterator<TableRow> rows, ContextualAttributesContextModel context )
	{
		configuration.getAttribute().forEach( attribute -> {
			// copy all to a contextual attribute
			ContextualAttribute contextualAttribute = new ContextualAttribute( attribute, context );
			if( isAttributeLocalized( contextualAttribute.getQualifier(), dataType ) )
			{
				final LocalizedAttributeInfo localizedAttributeInfo = new LocalizedAttributeInfo( parent, headerRow, rowsGroups.request(), configuration,
						contextualAttribute );
				this.renderLocalizedAttributeGroup( localizedAttributeInfo, data, dataType, widgetInstanceManager );
			}
			else
			{
				final TableRow row = rows.request();
				renderAttribute( parent, row, contextualAttribute, data, dataType, widgetInstanceManager );
				fireComponentRendered( row, parent, configuration, data );
			}
		} );
	}

	@Override
	protected Attribute buildAttributeWithLocalizedQualifierPath( Attribute attribute, Locale locale )
	{
		String localizedQualifier = ObjectValuePath.getLocalePath( attribute.getQualifier(), locale.toString() );
		Attribute localizedAttribute;
		if( attribute instanceof ContextualAttribute )
		{
			localizedAttribute = new ContextualAttribute( ((ContextualAttribute) attribute).getContext() );
		}
		else
		{
			localizedAttribute = new Attribute();
		}
		localizedAttribute.setQualifier( localizedQualifier );
		localizedAttribute.setPosition( attribute.getPosition() );
		localizedAttribute.setMergeMode( attribute.getMergeMode() );
		localizedAttribute.setLabel( attribute.getLabel() );
		localizedAttribute.setRenderer( attribute.getRenderer() );
		return localizedAttribute;
	}

	@Override
	protected void renderAttribute( TableRowsGroup parent, TableRow row, Attribute attribute, PartialRendererData<Collection> data, DataType dataType,
			WidgetInstanceManager widgetInstanceManager )
	{
		final ProxyRenderer<TableRow, Attribute, PartialRendererData<Collection>> proxyRenderer = new ProxyRenderer( this, parent, attribute, data );
		proxyRenderer.render( getAttributeRenderer( attribute ), row, attribute, data, dataType, widgetInstanceManager );
		updateTableRowDifferentMark( parent, row, attribute, data );
	}

	public WidgetComponentRenderer<TableRow, Attribute, PartialRendererData<Collection>> getAttributeRenderer()
	{
		return attributeRenderer;
	}

	@Required
	public void setAttributeRenderer( WidgetComponentRenderer<TableRow, Attribute, PartialRendererData<Collection>> attributeRenderer )
	{
		this.attributeRenderer = attributeRenderer;
	}

	public ContextualAttributesDao getContextualAttributesDao()
	{
		return contextualAttributesDao;
	}

	@Required
	public void setContextualAttributesDao( ContextualAttributesDao contextualAttributesDao )
	{
		this.contextualAttributesDao = contextualAttributesDao;
	}

	protected class ContextualAttribute extends Attribute
	{
		private ContextualAttributesContextModel context;

		public ContextualAttribute( final ContextualAttributesContextModel context )
		{
			this.context = context;
		}

		public ContextualAttribute( final Attribute attribute, final ContextualAttributesContextModel context )
		{
			this.setEditor( attribute.getEditor() );
			this.setLabel( attribute.getLabel() );
			this.setMergeMode( attribute.getMergeMode() );
			this.setQualifier( attribute.getQualifier() );
			this.setRenderer( attribute.getRenderer() );
			this.setPosition( attribute.getPosition() );
			this.getParameter().addAll( attribute.getParameter() );
			this.context = context;
		}

		public ContextualAttributesContextModel getContext()
		{
			return context;
		}
	}
}
