/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.file.routes;

import org.librairy.model.domain.resources.Resource;
import org.librairy.model.domain.resources.Source;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 07/04/16:
 *
 * @author cbadenes
 */
@Component
public class DefaultSource {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSource.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    @PostConstruct
    public void setup(){

        // Check if exists 'default' source
        if (udm.find(Resource.Type.SOURCE).by(Source.NAME, "default").isEmpty()){
            Source source = Resource.newSource("default");
            source.setUri(uriGenerator.from(Resource.Type.SOURCE, "default"));
            source.setDescription("default");
            source.setUrl("file://default");
            LOG.info("Creating default source: " + source);
            udm.save(source);
        }


    }
}