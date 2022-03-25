package de.hybris.contextualattributevaluesbackoffice.widgets.editors;

import de.hybris.contextualattributevaluesbackoffice.components.ContextualAttributeValuesEditor;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorUtils;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualEditorUtils
{
	public static String getEditorType( DataType type, Boolean contextual, Boolean simplifiedLocalized, Map<Pattern, String> customMappings )
	{
		String editorType = EditorUtils.getEditorType( type, simplifiedLocalized, customMappings );
		if( contextual )
		{
			if( editorType.contains( "Localized" ) )
			{
				return String.format( "Contextual%s", editorType );
			}
			else
			{
				return String.format( "ContextualSimple(%s)", editorType );
			}
		}
		return editorType;
	}

	public static Combobox createContextSelector( Div contextContainer, List<ContextualAttributesContextModel> currentContext, EditorContext editorContext )
	{
		Combobox contextSelector = new Combobox();
		contextSelector.setReadonly( true );
		contextSelector.setParent( contextContainer );
		ListModelList listModel = new ListModelList();
		listModel.addAll(
				(Collection< ? extends List<ContextualAttributesContextModel>>) editorContext.getParameter( ContextualAttributeValuesEditor.CONTEXTUAL_CONTEXTS ) );
		if( currentContext != null )
		{
			listModel.setSelection( Collections.singletonList( currentContext ) );
		}
		else
		{
			listModel.setSelection( Collections
					.singletonList( (List<ContextualAttributesContextModel>) editorContext.getParameter( ContextualAttributeValuesEditor.USER_GLOBAL_CONTEXTS ) ) );
		}
		contextSelector.setModel( listModel );
		contextSelector.setSclass( "ye-as-context-selector" );
		contextSelector.setItemRenderer( (ComboitemRenderer<List<ContextualAttributesContextModel>>) ( item, context, index ) -> {
			if( context.size() > 1 )
			{
				item.setLabel( "All available contexts" );
			}
			else if( context.size() == 1 )
			{
				item.setLabel( context.get( 0 ).getName() );
			}
			item.setValue( context );
		} );
		return contextSelector;
	}
}
