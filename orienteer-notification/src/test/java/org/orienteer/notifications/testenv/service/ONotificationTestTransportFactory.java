package org.orienteer.notifications.testenv.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.notifications.model.ONotificationTransport;
import org.orienteer.notifications.service.ONotificationTransportFactory;
import org.orienteer.notifications.testenv.OTestNotificationTransport;

public class ONotificationTestTransportFactory extends ONotificationTransportFactory {

  @Override
  public ONotificationTransport create(ODocument document) {
    if (document.getSchemaClass().getName().equalsIgnoreCase(OTestNotificationTransport.CLASS_NAME)) {
      return new OTestNotificationTransport(document);
    }
    return super.create(document);
  }
}