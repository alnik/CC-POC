package de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch.renderer.impl;

import de.hybris.contextualattributevaluesbackoffice.components.ContextualAttributeValuesEditor;
import de.hybris.contextualattributevaluesbackoffice.dataaccess.ContextualFulltextSearchStrategy;
import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualConditionValue;
import de.hybris.contextualattributevaluesbackoffice.widgets.editors.ContextualEditorUtils;
import de.hybris.platform.contextualattributevalues.daos.ContextualAttributesDao;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;

import com.google.common.collect.Lists;
import com.hybris.backoffice.widgets.fulltextsearch.FullTextSearchStrategy;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.FieldQueryFilter;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.impl.DefaultFieldQueryFieldRenderer;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.config.fulltextsearch.jaxb.FieldType;
import com.hybris.cockpitng.config.fulltextsearch.jaxb.FulltextSearch;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;

public class ContextualFieldQueryFieldRenderer extends DefaultFieldQueryFieldRenderer
{
	private static final Logger					LOGGER						= LoggerFactory.getLogger( ContextualFieldQueryFieldRenderer.class );
	private static final String					ATTRIBUTE_FILTER_EDITOR	= "filter-editor";
	private ContextualFulltextSearchStrategy	contextualFulltextSearchStrategy;
	private ContextualAttributesDao				contextualAttributesDao;
	private UserService								userService;

	protected void renderEditor( final Component parent, final WidgetInstanceManager widgetInstanceManager ) throws TypeNotFoundException
	{
		final FulltextSearch configuration = getFulltextSearchConfig( parent );
		final FullTextSearchStrategy fullTextSearchStrategy = getSearchStrategy( widgetInstanceManager );
		final FieldQueryFilter data = getFieldQueryData( parent );
		if(data.getValue() == null && !data.isEnabled())
		{
			data.setEnabled(true);
		}
		final FieldType fieldType = findField( configuration, data.getName() );
		if( fieldType == null )
		{
			LOGGER.error( "Unable to determine field configuration for field '{}' in '{}'", data.getName(), widgetInstanceManager.getWidgetslot().getId() );
			return;
		}

		final String editorProperty = getEditorProperty( data );
		final String valueType = fullTextSearchStrategy.getFieldType( data.getSearchData().getTypeCode(), data.getName() );
		if( valueType == null )
		{
			LOGGER.error( "Unable to determine type for field '{}' in '{}'", data.getName(), widgetInstanceManager.getWidgetslot().getId() );
			return;
		}

		final DataType dataType = getTypeFacade().load( valueType );
		// change editor to contextual one
		final ContextualAttributeValuesEditor editor = new ContextualAttributeValuesEditor();
		final boolean localized = fullTextSearchStrategy.isLocalized( data.getSearchData().getTypeCode(), data.getName() );
		if( localized )
		{
			final Set<Locale> writableLocales = getPermissionFacade().getAllWritableLocalesForCurrentUser();
			final Set<Locale> readableLocales = new HashSet<>( getPermissionFacade().getAllReadableLocalesForCurrentUser() );
			readableLocales.addAll( writableLocales );
			editor.setWritableLocales( writableLocales );
			editor.setReadableLocales( readableLocales );
		}
		boolean contextual = false;
		if( fullTextSearchStrategy instanceof ContextualFulltextSearchStrategy )
		{
			contextual = ((ContextualFulltextSearchStrategy) fullTextSearchStrategy).isContextual( data.getSearchData().getTypeCode(), data.getName() );
			if( contextual )
			{
				editor.setContexts( getAvailableContexts() );
				editor.setUserGlobalContext( getAvailableContexts().get( 0 ) );
			}
		}
		// assume all
		fieldType.getEditorParameter().stream().filter( parameter -> StringUtils.isNotBlank( parameter.getName() ) )
				.forEach( parameter -> editor.addParameter( parameter.getName(), parameter.getValue() ) );
		editor.setWidgetInstanceManager( widgetInstanceManager );
		editor.setProperty( editorProperty );
		editor.setType( ContextualEditorUtils.getEditorType( dataType, contextual, localized ? Boolean.TRUE : null, prepareEditorMappings() ) );
		editor.setLocalized( localized );
		editor.setOptional( true );
		editor.setDefaultEditor( fieldType.getEditor() );
		editor.setAtomic( dataType.isAtomic() );
		editor.setReadOnly( !data.getOperator().isRequireValue() );
		widgetInstanceManager.getModel().setValue( editorProperty, data.getValue() );
		editor.initialize();
		editor.addEventListener( Editor.ON_VALUE_CHANGED, event -> onValueChanged( data, editor.getValue() ) );
		if( localized || contextual )
		{
			editor.addEventListener( Events.ON_SELECT, event -> onSelectChanged( data, event.getData() ) );
		}
		parent.setAttribute( ATTRIBUTE_FILTER_EDITOR, editor );
		parent.appendChild( editor );
		fireComponentRendered( editor, parent, configuration, data );
	}


	private void onSelectChanged( FieldQueryFilter data, Object change )
	{
		onLanguageChanged( data, change );
		if( data instanceof ContextualFieldQueryFilter && isContextChange( change ) )
		{
			((ContextualFieldQueryFilter) data).setContext( (List<ContextualAttributesContextModel>) change );
		}
	}

	private boolean isContextChange( Object change )
	{
		return change instanceof List && ((List) change).size() > 0 && ((List) change).get( 0 ) instanceof ContextualAttributesContextModel;
	}

	@Override
	protected void onLanguageChanged( FieldQueryFilter data, Object eventData )
	{
		if( eventData instanceof Locale && data.getValue() instanceof ContextualConditionValue )
		{
			final ContextualConditionValue initialValue = (ContextualConditionValue) data.getValue();
			data.setLocale( (Locale) eventData );
			if( initialValue == null || !initialValue.getLocale().equals( eventData ) )
			{
				data.setEnabled( false );
			}
			else
			{
				data.setEnabled( StringUtils.isNotEmpty( (CharSequence) initialValue.getValue() ) );
			}
		}
		else
		{
			super.onLanguageChanged( data, eventData );
		}
	}

	private List<List<ContextualAttributesContextModel>> getAvailableContexts()
	{
		List contextListModelList = new ArrayList();
		List<ContextualAttributesContextModel> allowedContexts = getAllowedContextsForCurrentUser();
		// add one item for all contexts = global search
		contextListModelList.add( Lists.newArrayList( allowedContexts ) );
		if( allowedContexts.size() > 1 )
		{
			allowedContexts.forEach( context -> contextListModelList.add( Collections.singletonList( context ) ) );
		}
		return contextListModelList;
	}

	private List<ContextualAttributesContextModel> getAllowedContextsForCurrentUser()
	{
		if( userService.isAdmin( userService.getCurrentUser() ) )
		{
			return getContextualAttributesDao().findAllAvailableContexts();
		}
		else
		{
			EmployeeModel employeeModel = (EmployeeModel) userService.getCurrentUser();
			return employeeModel.getManagedStores().stream().map( baseStoreModel -> baseStoreModel.getContextualAttributesContext() )
					.collect( Collectors.toList() );
		}
	}

	@Override
	protected void onFilterChanged(FieldQueryFilter data, String name) {
		super.onFilterChanged(data, name);
		data.setEnabled(true);
	}

	public ContextualFulltextSearchStrategy getContextualFulltextSearchStrategy()
	{
		return contextualFulltextSearchStrategy;
	}

	@Required
	public void setContextualFulltextSearchStrategy( ContextualFulltextSearchStrategy contextualFulltextSearchStrategy )
	{
		this.contextualFulltextSearchStrategy = contextualFulltextSearchStrategy;
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

	@Required
	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}
}
