/**
 * Copyright (C) 2015 Ilia Naryzhny (phantom@ydn.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orienteer.components.table;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.CustomAttributes;
import org.orienteer.components.properties.AbstractMetaPanel;
import org.orienteer.components.properties.DisplayMode;
import org.orienteer.components.properties.OClassMetaPanel;

import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;

import com.orientechnologies.orient.core.metadata.schema.OClass;

public class OClassMetaColumn extends AbstractModeMetaColumn<OClass, DisplayMode, String, String> {

    public OClassMetaColumn(CustomAttributes custom, IModel<DisplayMode> modeModel) {
        this(custom.getName(), modeModel);
    }

    public OClassMetaColumn(String critery, IModel<DisplayMode> modeModel) {
        this(critery, critery, modeModel);
    }

    public OClassMetaColumn(String sortParam, String critery, IModel<DisplayMode> modeModel) {
        super(sortParam, Model.of(critery), modeModel);
    }

    @Override
    protected <V> AbstractMetaPanel<OClass, String, V> newMetaPanel(
            String componentId, IModel<String> criteryModel,
            IModel<OClass> rowModel) {
        return new OClassMetaPanel<V>(componentId, getModeModel(), rowModel, criteryModel);
    }

    @Override
    protected IModel<String> newLabelModel() {
        return new SimpleNamingModel<String>("class", getCriteryModel());
    }

}
