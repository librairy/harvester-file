/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.file.eventbus;

import com.google.common.base.Strings;
import org.librairy.harvester.file.services.ItemService;
import org.librairy.model.Event;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.BindingKey;
import org.librairy.model.modules.EventBus;
import org.librairy.model.modules.EventBusSubscriber;
import org.librairy.model.modules.RoutingKey;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by cbadenes on 01/12/15.
 */
@Component
public class ItemCreatedEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(ItemCreatedEventHandler.class);

    @Autowired
    ItemService itemService;

    @Autowired
    UDM udm;

    @Autowired
    protected EventBus eventBus;

    @PostConstruct
    public void init(){
        RoutingKey routingKey = RoutingKey.of(Resource.Type.ITEM, Resource.State.CREATED);
        LOG.info("Trying to register as subscriber of '" + routingKey + "' events ..");
        eventBus.subscribe(this, BindingKey.of(routingKey, "harvester-item"));
        LOG.info("registered successfully");
    }


    @Override
    public void handle(Event event) {
        LOG.debug("New Item event received: " + event);
        try{

            Resource resource = event.to(Resource.class);

            Item item = udm.read(Resource.Type.ITEM).byUri(resource.getUri()).get().asItem();
            if (Strings.isNullOrEmpty(item.getTokens())){
                itemService.tokenize(item);
            }
        } catch (RuntimeException e){
            LOG.warn(e.getMessage());
        }catch (Exception e){
            LOG.error("Error adding new source: " + event, e);
        }
    }
}