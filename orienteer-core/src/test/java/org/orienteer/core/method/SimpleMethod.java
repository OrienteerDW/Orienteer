package org.orienteer.core.method;

import org.orienteer.core.component.command.Command;
import org.orienteer.core.method.definitions.JavaClassOMethodDefinition;
import org.orienteer.core.method.definitions.JavaClassOMethodDefinitionStorage;

/**
 * 
 * This method annotated NOT as {@link JavaClassOMethodDefinition} and NOT loaded into {@link JavaClassOMethodDefinitionStorage} 
 *
 */

public class SimpleMethod implements IMethod {

	@Override
	public void init(IMethodDefinition config, IMethodContext envData) {
	}

	@Override
	public Command<?> createCommand(String id) {
		return null;
	}


}
