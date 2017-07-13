package org.orienteer.taucharts.component.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.orienteer.core.widget.Widget;
import org.orienteer.taucharts.component.TauchartsConfig;
import org.orienteer.taucharts.component.TauchartsPanel;

import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Widget displays {@link TauchartsPanel} on document page
 * All document properties can be getting in SQL query as ":property_name" 
 */
@Widget(id="taucharts-document", domain="document", oClass=AbstractTauchartsWidget.WIDGET_OCLASS_NAME, order=10, autoEnable=false)
public class TauchartsDocumentWidget extends AbstractTauchartsWidget<ODocument>{
	private static final long serialVersionUID = 1L;

	public TauchartsDocumentWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
	}
	
	@Override
	protected void makeChartPanel(){
		List<String> plugins = new ArrayList<String>();
		Set<ODocument> pluginsLinks = getWidgetDocument().field(PLUGINS_PROPERTY_NAME);
		if (pluginsLinks!=null){
			for (ODocument oDocument : pluginsLinks) {
				plugins.add((String) oDocument.field("alias"));
			}
		}
		add(new TauchartsPanel(
				"tauchart",
				getModel(),
				new TauchartsConfig(
					(String)(((ODocument) getWidgetDocument().field(TYPE_PROPERTY_NAME)).field("alias")),
					(String)getWidgetDocument().field(X_PROPERTY_NAME),
					(String)getWidgetDocument().field(Y_PROPERTY_NAME),
					(String)getWidgetDocument().field(COLOR_PROPERTY_NAME),
					plugins,
					(String)getWidgetDocument().field(QUERY_PROPERTY_NAME),
					(String) getWidgetDocument().field(X_LABEL_PROPERTY_NAME),
					(String) getWidgetDocument().field(Y_LABEL_PROPERTY_NAME),
					(Boolean) getWidgetDocument().field(USING_REST_PROPERTY_NAME)
				)
		));		
	}
}
