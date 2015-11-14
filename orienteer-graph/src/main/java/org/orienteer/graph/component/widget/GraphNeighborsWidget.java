package org.orienteer.graph.component.widget;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.DeleteODocumentCommand;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.table.OEntityColumn;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.model.ODocumentNameModel;
import org.orienteer.core.service.impl.OClassIntrospector;
import org.orienteer.core.widget.AbstractModeAwareWidget;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.graph.component.command.CreateEdgeCommand;

import org.orienteer.graph.component.command.CreateVertexCommand;
import org.orienteer.graph.component.command.DeleteEdgeCommand;
import org.orienteer.graph.component.command.DeleteVertexCommand;
import ru.ydn.wicket.wicketorientdb.model.OClassModel;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

/**
 * Widget for displaying vertex neighbors.
 */
@Widget(id="neighbors", domain="document", order=10, autoEnable=true, selector="V")
public class GraphNeighborsWidget extends AbstractModeAwareWidget<ODocument> {

    @Inject
    private OClassIntrospector oClassIntrospector;

    public GraphNeighborsWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);

        IModel<DisplayMode> modeModel = DisplayMode.VIEW.asModel();
        Form<ODocument> form = new Form<ODocument>("form");
        OQueryDataProvider<ODocument> provider = new OQueryDataProvider<ODocument>("select expand(both()) from "+getModelObject().getIdentity());
        OClass commonParent = provider.probeOClass(20);
        List<IColumn<ODocument, String>> columns = oClassIntrospector.getColumnsFor(commonParent, true, getModeModel());

        OrienteerDataTable<ODocument, String> table =
            new OrienteerDataTable<ODocument, String>("neighbors", columns, provider, 20);
        table.addCommand(new CreateVertexCommand(table, getModel()));
        table.addCommand(new CreateEdgeCommand(table, getModel()));
        table.addCommand(new DeleteEdgeCommand(table, getModel()));
        table.addCommand(new DeleteVertexCommand(table, getModel()));
        form.add(table);
        add(form);
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.arrows_h);
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("widget.document.neighbours.title", new ODocumentNameModel(getModel()));
    }

    @Override
    protected String getWidgetStyleClass() {
        return "strict";
    }
}