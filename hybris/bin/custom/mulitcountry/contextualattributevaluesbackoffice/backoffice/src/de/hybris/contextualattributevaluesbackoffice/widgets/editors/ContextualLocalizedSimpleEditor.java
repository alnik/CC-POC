package de.hybris.contextualattributevaluesbackoffice.widgets.editors;

import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualConditionValue;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.editor.localized.AbstractLocalizedEditor;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.i18n.LocalizedValuesService;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualLocalizedSimpleEditor extends AbstractLocalizedEditor
{
	private LocalizedValuesService localizedValuesService;

	public ContextualLocalizedSimpleEditor()
	{
	}

	public void render( Component parent, EditorContext editorContext, EditorListener editorListener )
	{
		Validate.notNull( "All parameters are mandatory", parent, editorContext, editorListener );
		ContextualConditionValue initialValue = (ContextualConditionValue) ObjectUtils.defaultIfNull( editorContext.getInitialValue(),
				new ContextualConditionValue() );
		if( parent instanceof Editor )
		{
			this.setWidgetInstanceManager( ((Editor) parent).getWidgetInstanceManager() );
		}
		Locale currentLocale = initialValue.getLocale() != null ? initialValue.getLocale() : getCockpitLocaleService().getCurrentLocale();
		Object currentValue = initialValue.getValue();
		List<ContextualAttributesContextModel> currentContext = initialValue.getContext();
		Div editorLabel = this.createEditorLabel( editorContext );
		parent.appendChild( editorLabel );
		Div contextContainer = new Div();
		contextContainer.setSclass( "yw-advancedsearch-context" );
		Combobox contextSelector = ContextualEditorUtils.createContextSelector( contextContainer, currentContext, editorContext );
		contextContainer.setParent( parent );
		contextSelector.addEventListener( "onSelect", ( event ) -> {
			List<ContextualAttributesContextModel> selectedContext = this.getSelectedContext( contextSelector );
			initialValue.setContext( selectedContext );
			Events.postEvent( "onSelect", parent, selectedContext );
		} );
		Div langContainer = new Div();
		langContainer.setSclass( "yw-advancedsearch-local" );
		Combobox langSelector = this.createLangSelector( langContainer, currentLocale, editorContext );
		langContainer.setParent( parent );
		langSelector.addEventListener( "onOK", ( event ) -> {
			editorListener.onEditorEvent( "onOK" );
		} );
		Div editorContainer = new Div();
		editorContainer.setParent( parent );
		editorContainer.setSclass( "yw-advancedsearch-local-editor" );
		Editor editor = this.prepareSubEditor( editorContext, currentValue );
		editor.addEventListener( "onValueChanged", ( event ) -> {
			initialValue.setLocale( this.getSelectedLocale( langSelector ) );
			initialValue.setValue( event.getData() );
			initialValue.setContext( this.getSelectedContext( contextSelector ) );
			editorListener.onValueChanged( initialValue );
		} );
		editor.addEventListener( "onEditorEvent", ( event ) -> {
			editorListener.onEditorEvent( (String) event.getData() );
		} );
		langSelector.addEventListener( "onSelect", ( event ) -> {
			Locale selectedLocale = this.getSelectedLocale( langSelector );
			initialValue.setLocale( selectedLocale );
			editorListener.onValueChanged( initialValue );
			editor.setValue( initialValue.getValue() );
			Events.postEvent( "onSelect", parent, selectedLocale );
		} );
		editor.afterCompose();
		editorContainer.appendChild( editor );
	}

	private List<ContextualAttributesContextModel> getSelectedContext( Combobox contextSelector )
	{
		ListModelList<List<ContextualAttributesContextModel>> contextSelectorModel = (ListModelList) contextSelector.getModel();
		return contextSelectorModel != null && CollectionUtils.isNotEmpty( contextSelectorModel.getSelection() )
				? contextSelectorModel.getSelection().iterator().next()
				: null;
	}

	private List<Locale> getEffectiveLocales( EditorContext editorContext )
	{
		List<Locale> effectiveLocales = Lists.newArrayList( this.getActiveLocales() );
		effectiveLocales.retainAll( (Collection) ObjectUtils.defaultIfNull( editorContext.getReadableLocales(), Collections.emptySet() ) );
		return effectiveLocales;
	}

	private Combobox createLangSelector( Div langContainer, Locale selectedLocale, EditorContext editorContext )
	{
		Combobox langSelector = new Combobox();
		langSelector.setReadonly( true );
		langSelector.setParent( langContainer );
		ListModelList<Locale> listModel = new ListModelList();
		listModel.addAll( this.getEffectiveLocales( editorContext ) );
		if( selectedLocale != null )
		{
			listModel.setSelection( Collections.singletonList( selectedLocale ) );
		}
		langSelector.setModel( listModel );
		langSelector.setSclass( "ye-as-lang-selector" );
		langSelector.setItemRenderer( new ComboitemRenderer<Locale>()
		{
			public void render( Comboitem item, Locale locale, int index )
			{
				item.setLabel( getLocalizedValuesService().getLanguageLabelKey( locale ) );
				item.setValue( locale );
			}
		} );
		return langSelector;
	}

	private Locale getSelectedLocale( Combobox langSelector )
	{
		ListModelList<Locale> langSelectorModel = (ListModelList) langSelector.getModel();
		return langSelectorModel != null && CollectionUtils.isNotEmpty( langSelectorModel.getSelection() )
				? (Locale) langSelectorModel.getSelection().iterator().next()
				: null;
	}

	public LocalizedValuesService getLocalizedValuesService()
	{
		if( this.localizedValuesService == null )
		{
			this.localizedValuesService = (LocalizedValuesService) SpringUtil.getBean( "localizedValuesService" );
		}
		return this.localizedValuesService;
	}

	public void setLocalizedValuesService( LocalizedValuesService localizedValuesService )
	{
		this.localizedValuesService = localizedValuesService;
	}
}
