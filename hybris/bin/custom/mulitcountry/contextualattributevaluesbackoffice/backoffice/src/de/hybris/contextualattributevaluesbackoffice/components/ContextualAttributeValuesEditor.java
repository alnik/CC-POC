package de.hybris.contextualattributevaluesbackoffice.components;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.List;

import com.hybris.cockpitng.components.Editor;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualAttributeValuesEditor extends Editor
{
	public static final String	CONTEXTUAL_CONTEXTS	= "contextualContexts";
	public static final String	USER_GLOBAL_CONTEXTS	= "userGlobalContext";

	public ContextualAttributeValuesEditor()
	{
	}

	public void setContexts( List<List<ContextualAttributesContextModel>> contexts )
	{
		setAttribute( CONTEXTUAL_CONTEXTS, contexts );
	}

	public void setUserGlobalContext( List<ContextualAttributesContextModel> context )
	{
		setAttribute( USER_GLOBAL_CONTEXTS, context );
	}
}
