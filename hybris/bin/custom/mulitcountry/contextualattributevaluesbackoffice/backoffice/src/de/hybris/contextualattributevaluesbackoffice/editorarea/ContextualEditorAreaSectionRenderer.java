package de.hybris.contextualattributevaluesbackoffice.editorarea;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.util.ContextualAttributeValuesUtil;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tabpanel;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.common.EditorBuilder;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractSection;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Parameter;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Section;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.util.YTestTools;
import com.hybris.cockpitng.widgets.common.ProxyRenderer;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import com.hybris.cockpitng.widgets.editorarea.renderer.EditorAreaRendererUtils;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaSectionRenderer;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualEditorAreaSectionRenderer extends DefaultEditorAreaSectionRenderer
{
	Logger					LOG	= Logger.getLogger( ContextualEditorAreaSectionRenderer.class );
	private ObjectFacade	objectFacade;

	public void render( Component parent, AbstractSection abstractSectionConfiguration, Object object, DataType dataType,
			WidgetInstanceManager widgetInstanceManager )
	{
		if( object instanceof ProductModel )
		{
			for( ContextualAttributeValueModel contextualAttributeValueModel: ((ProductModel) object).getContextualAttributeValues() )
			{
				Groupbox sectionGrpBox = this.prepareContainer( parent, abstractSectionConfiguration, contextualAttributeValueModel, widgetInstanceManager );
				sectionGrpBox.setAttribute( "componentCtx", abstractSectionConfiguration );
				Label description = this.renderDescription( abstractSectionConfiguration, sectionGrpBox );
				if( description != null )
				{
					this.fireComponentRendered( description, parent, abstractSectionConfiguration, object );
				}
				WidgetComponentRenderer<Component, Section, Object> renderer = this.createSectionRenderer();
				(new ProxyRenderer( this, parent, abstractSectionConfiguration, object )).render( renderer, sectionGrpBox, abstractSectionConfiguration,
						contextualAttributeValueModel, dataType, widgetInstanceManager );
				this.fireComponentRendered( sectionGrpBox, parent, abstractSectionConfiguration, object );
				// registerContextualAttributeValueAfterSaveListener( widgetInstanceManager, contextualAttributeValueModel, parent );
			}
			registerAfterSaveListener( widgetInstanceManager, parent, (ProductModel) object );
		}
	}

	private void registerAfterSaveListener( final WidgetInstanceManager widgetInstanceManager, final Component parent, ProductModel productModel )
	{
		EditorAreaRendererUtils.setAfterSaveListener( widgetInstanceManager.getModel(), "reloadTabListener", ( event ) -> {
			refreshCurrentTab( parent );
		}, true );
	}

	private void refreshCurrentTab( Component component )
	{
		Tabpanel tabPanel = this.findClosestTabPanel( component );
		if( tabPanel != null )
		{
			Events.postEvent( new Event( "onTabSelected", tabPanel ) );
		}
	}

	private Tabpanel findClosestTabPanel( Component component )
	{
		if( component instanceof Tabpanel )
		{
			return (Tabpanel) component;
		}
		else
		{
			return component != null ? this.findClosestTabPanel( component.getParent() ) : null;
		}
	}

	private void registerContextualAttributeValueAfterSaveListener( final WidgetInstanceManager widgetInstanceManager,
			final ContextualAttributeValueModel contextualAttributeValueModel, final Component parent )
	{
		EditorAreaRendererUtils.setAfterSaveListener( widgetInstanceManager.getModel(),
				"contextualAttributeValuesAfterSaveListener." + contextualAttributeValueModel.getPk(), ( event ) -> {
					if( contextualAttributeValueModel.getItemModelContext().isDirty() )
					{
						LOG.info( "Save contextual values with pk " + contextualAttributeValueModel.getPk() );
						saveContextualAttributeValue( contextualAttributeValueModel, widgetInstanceManager );
						this.refreshCurrentTab( parent );
					}
				}, true );
	}

	private void saveContextualAttributeValue( final ContextualAttributeValueModel contextualAttributeValueModel,
			final WidgetInstanceManager widgetInstanceManager )
	{
		if( ContextualAttributeValuesUtil.canEditContextualAttributeValue( contextualAttributeValueModel ) )
		{
			try
			{
				objectFacade.save( contextualAttributeValueModel );
				ProductModel productModel = contextualAttributeValueModel.getProduct();
				productModel.setModifiedtime( new Date() );
				widgetInstanceManager.getModel().setValue( "currentObject", objectFacade.save( productModel ) );
			}
			catch( ObjectSavingException e )
			{
				LOG.error("Cannot save contextual attribute values for product with code " + contextualAttributeValueModel.getProduct().getCode(), e);
			}
		}
	}

	private Groupbox prepareContainer( Component parent, AbstractSection abstractSectionConfiguration, ContextualAttributeValueModel object,
			WidgetInstanceManager widgetInstanceManager )
	{
		Groupbox sectionGrpBox = new Groupbox();
		UITools.modifySClass( sectionGrpBox, "yw-editorarea-tabbox-tabpanels-tabpanel-groupbox", true );
		if( this.isEssentialSection( abstractSectionConfiguration ) )
		{
			UITools.modifySClass( sectionGrpBox, "yw-editorarea-tabbox-tabpanels-tabpanel-groupbox-essential", true );
		}
		sectionGrpBox.setParent( parent );
		this.setSectionOpenAttribute( abstractSectionConfiguration, sectionGrpBox, widgetInstanceManager );
		Caption caption = this.prepareContainerCaption( abstractSectionConfiguration, object );
		if( caption != null )
		{
			sectionGrpBox.appendChild( caption );
			Button expandButton = new Button();
			expandButton.setSclass( "yw-expandCollapse" );
			expandButton.addEventListener( "onClick", ( e ) -> {
				sectionGrpBox.setOpen( !sectionGrpBox.isOpen() );
			} );
			caption.appendChild( expandButton );
			this.fireComponentRendered( caption, parent, abstractSectionConfiguration, object );
		}
		YTestTools.modifyYTestId( sectionGrpBox, abstractSectionConfiguration.getName() );
		return sectionGrpBox;
	}

	protected Caption prepareContainerCaption( AbstractSection abstractSectionConfiguration, ContextualAttributeValueModel contextualAttributeValueModel )
	{
		String sectionName = this.resolveLabel( abstractSectionConfiguration.getName() );
		sectionName += " [" + contextualAttributeValueModel.getContext().getName() + "]";
		Caption caption = new Caption( sectionName );
		YTestTools.modifyYTestId( caption, abstractSectionConfiguration.getName() + "_caption" );
		caption.setSclass( "yw-editorarea-tabbox-tabpanels-tabpanel-groupbox-caption" );
		if( this.isEssentialSection( abstractSectionConfiguration ) )
		{
			UITools.modifySClass( caption, "yw-editorarea-tabbox-tabpanels-tabpanel-groupbox-caption-essential", true );
		}
		return caption;
	}

	protected Editor createEditor( DataType genericType, WidgetInstanceManager widgetInstanceManager, Attribute attribute, Object object )
	{
		DataAttribute genericAttribute = genericType.getAttribute( attribute.getQualifier() );
		if( genericAttribute == null )
		{
			return null;
		}
		else
		{
			String qualifier = genericAttribute.getQualifier();
			boolean editable = !attribute.isReadonly() && this.canChangeProperty( genericAttribute, object );
			String editorSClass = this.getEditorSClass( editable );
			boolean editorValueDetached = this.isEditorValueDetached( attribute );
			EditorBuilder editorBuilder = this.getEditorBuilder( widgetInstanceManager )
					.addParameters( attribute.getEditorParameter().stream(), this::extractParameterName, this::extractParameterValue )
					.useEditor( attribute.getEditor() ).setValueType( this.resolveEditorType( genericAttribute ) )
					.configure( getItemKey( "currentObject", object ), genericAttribute, editorValueDetached ).setReadOnly( !editable )
					.setLabel( this.resolveAttributeLabel( attribute, genericType ) ).setDescription( this.getAttributeDescription( genericType, attribute ) )
					.useEditor( attribute.getEditor() ).setValueType( this.resolveEditorType( genericAttribute ) ).apply( ( editorx ) -> {
						editorx.setSclass( editorSClass );
					} ).apply( ( editorx ) -> {
						this.processEditorBeforeComposition( editorx, genericType, widgetInstanceManager, attribute, object );
					} );
			Editor editor = this.buildEditor( editorBuilder, widgetInstanceManager );
			YTestTools.modifyYTestId( editor, "editor_" + qualifier );
			return editor;
		}
	}

	private String getItemKey( final String currentObject, final Object object )
	{
		if( object instanceof ContextualAttributeValueModel )
		{
			int index = Lists.newArrayList( ((ContextualAttributeValueModel) object).getProduct().getContextualAttributeValues() ).indexOf( object );
			return currentObject + ".contextualAttributeValues[" + index + "]";
		}
		else
		{
			return currentObject;
		}
	}

	private String getEditorSClass( boolean editable )
	{
		return editable ? "yw-editorarea-tabbox-tabpanels-tabpanel-groupbox-ed-editor" : "ye-default-editor-readonly";
	}

	private boolean isEditorValueDetached( Attribute attribute )
	{
		return (Boolean) attribute.getEditorParameter().stream().filter( ( parameter ) -> {
			return parameter.getName().equals( "attributeValueDetached" );
		} ).map( Parameter::getValue ).map( Boolean::valueOf ).findAny().orElse( Boolean.FALSE );
	}

	@Required
	public void setObjectFacade( final ObjectFacade objectFacade )
	{
		this.objectFacade = objectFacade;
	}
}
