/**
 *
 */
package de.hybris.cockpitng.editor.multicountryextendedmultireferenceeditor;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListitemRenderer;

import com.hybris.cockpitng.common.configuration.EditorConfigurationUtil;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorLayout;
import com.hybris.cockpitng.editor.extendedmultireferenceeditor.DefaultExtendedMultiReferenceEditor;
import com.hybris.cockpitng.editor.extendedmultireferenceeditor.InlineEditorHeader;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * @author i304602
 *
 */
public class McExtendedMultiReferenceEditor<T> extends DefaultExtendedMultiReferenceEditor<T>
{

	@SuppressWarnings("deprecation")
	protected boolean inlineEditingEnabled(final EditorContext<Collection<T>> context)
	{
		return context != null && context.isEditable()
				&& ObjectUtils.equals(context.getParameter("inlineEditing"), Boolean.TRUE.toString());
	}

	@Override
	public ReferenceEditorLayout<T> createReferenceLayout(final EditorContext context)
	{
		final WidgetInstanceManager widgetInstanceManager = (WidgetInstanceManager) context.getParameterAs("wim");
		final List<ListColumn> columns = EditorConfigurationUtil.getColumns(context, widgetInstanceManager, getTypeCode());

		return new ReferenceEditorLayout<T>(this,
				EditorConfigurationUtil.getBaseConfiguration(widgetInstanceManager, getTypeCode()))
		{

			@Override
			public void createLayout(final Component parent)
			{
				super.createLayout(parent);

				final InlineEditorHeader header = createSelectedListHeader(getCurrentlySelectedList(), columns);
				if (inlineEditingEnabled(context))
				{
					prepareGlobalValidationContainer(getCurrentlySelectedList(), header);
				}
			}

			@Override
			protected ListitemRenderer createSelectedItemsListItemRenderer()
			{
				assignCurrentObjectToRootType(widgetInstanceManager);
				// we need to pass modified editor property because getParentEditor is protected so it is not available
				// inside of InlineRowRenderer

				return new McRowRenderer(McExtendedMultiReferenceEditor.this, getSelectedElementsListModel(),
						getInlineProperty());
			}


			private void assignCurrentObjectToRootType(final WidgetInstanceManager widgetInstanceManager)
			{
				final int index = getInlineProperty().indexOf(ATTRIBUTE_DELIMITER);
				if (index > 0)
				{
					final String typeKey = getInlineProperty().substring(0, index);
					final Object currentObject = context.getParameter(PARENT_OBJECT);
					widgetInstanceManager.getModel().put(typeKey, currentObject);
				}
			}


		};
	}



}
