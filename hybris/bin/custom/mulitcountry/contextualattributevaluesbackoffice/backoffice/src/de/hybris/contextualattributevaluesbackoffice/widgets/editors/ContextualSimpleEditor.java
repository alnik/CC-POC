package de.hybris.contextualattributevaluesbackoffice.widgets.editors;

import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualConditionValue;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.editors.impl.AbstractCockpitEditorRenderer;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.engine.WidgetInstanceManagerAware;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualSimpleEditor extends AbstractCockpitEditorRenderer<Object> implements WidgetInstanceManagerAware
{
	private WidgetInstanceManager widgetInstanceManager;

	public void render( Component parent, EditorContext editorContext, EditorListener editorListener )
	{
		Validate.notNull( "All parameters are mandatory", parent, editorContext, editorListener );
		ContextualConditionValue initialValue = (ContextualConditionValue) ObjectUtils.defaultIfNull( editorContext.getInitialValue(),
				new ContextualConditionValue() );
		if( parent instanceof Editor )
		{
			this.setWidgetInstanceManager( ((Editor) parent).getWidgetInstanceManager() );
		}
		Object currentValue = initialValue.getValue();
		List<ContextualAttributesContextModel> currentContext = initialValue.getContext();
		Div contextContainer = new Div();
		contextContainer.setSclass( "yw-advancedsearch-context" );
		Combobox contextSelector = ContextualEditorUtils.createContextSelector( contextContainer, currentContext, editorContext );
		contextContainer.setParent( parent );
		Div editorContainer = new Div();
		editorContainer.setParent( parent );
		Editor editor = this.prepareSubEditor( editorContext, currentValue );
		contextSelector.addEventListener( "onSelect", ( event ) -> {
			List<ContextualAttributesContextModel> selectedContext = this.getSelectedContext( contextSelector );
			initialValue.setContext( selectedContext );
			editor.setValue( initialValue.getValue() );
			editorListener.onValueChanged( initialValue );
			Events.postEvent( "onSelect", parent, selectedContext );
		} );
		editor.addEventListener( "onEditorEvent", ( event ) -> {
			editorListener.onEditorEvent( (String) event.getData() );
		} );
		editor.addEventListener( "onValueChanged", ( event ) -> {
			initialValue.setValue( event.getData() );
			initialValue.setContext( this.getSelectedContext( contextSelector ) );
			editorListener.onValueChanged( initialValue );
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

	protected Editor prepareSubEditor( EditorContext<Object> editorContext, Object currentValue )
	{
		Editor subEditor = new Editor();
		subEditor.setReadOnly( !editorContext.isEditable() );
		subEditor.setInitialValue( currentValue );
		subEditor.setOptional( editorContext.isOptional() );
		subEditor.setWidgetInstanceManager( this.widgetInstanceManager );
		subEditor.setOrdered( editorContext.isOrdered() );
		subEditor.setWritableLocales( editorContext.getWritableLocales() );
		subEditor.setReadableLocales( editorContext.getReadableLocales() );
		Map<String, Object> parameters = editorContext.getParameters();
		String defaultEditor = (String) editorContext.getParameterAs( "valueEditor" );
		if( StringUtils.isNotBlank( defaultEditor ) )
		{
			String embeddedEditor = this.extractEmbeddedEditor( defaultEditor );
			parameters.put( "valueEditor", embeddedEditor );
			subEditor.setDefaultEditor( embeddedEditor );
		}
		subEditor.setType( this.extractEmbeddedType( editorContext ) );
		subEditor.addParameters( parameters );
		return subEditor;
	}

	@Override
	public void setWidgetInstanceManager( WidgetInstanceManager widgetInstanceManager )
	{
		this.widgetInstanceManager = widgetInstanceManager;
	}
}
