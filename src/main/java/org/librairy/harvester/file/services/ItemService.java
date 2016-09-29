/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.file.services;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.librairy.harvester.file.executor.ParallelExecutor;
import org.librairy.harvester.file.helper.LanguageHelper;
import org.librairy.harvester.file.parser.ParsedDocument;
import org.librairy.harvester.file.parser.Parser;
import org.librairy.harvester.file.tokenizer.Language;
import org.librairy.harvester.file.tokenizer.Tokenizer;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 11/02/16.
 */
@Component
public class ItemService {

    private static final Logger LOG = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    @Autowired
    Tokenizer tokenizer;

    @Autowired
    Parser parser;

    private ParallelExecutor executor;

    @PostConstruct
    public void setup(){
        this.executor = new ParallelExecutor();
    }

    public void handleParallel(Resource resource){
        executor.execute(() -> handle(resource));
    }

    public void handle(Resource resource){

        try{
            //Read document from db
            Optional<Resource> res = udm.read(Resource.Type.DOCUMENT).byUri(resource.getUri());

            if (!res.isPresent()){
                LOG.warn("Received event of unknown resource: " + resource.getUri());
                return;
            }

            Document document = (Document) res.get();

            // Parsing File of the Document
            File file = new File(document.getRetrievedFrom());
            ParsedDocument parsedDocument = parser.parse(file);

            Language language = LanguageHelper.getLanguageFrom(file);

            // -> Textual Item
            String textualContent = parsedDocument.getText();
            if (!Strings.isNullOrEmpty(textualContent)){
                Item textualItem = createItem(textualContent, "text", file.getAbsolutePath(),language);
                udm.save(textualItem);
                udm.save(Relation.newBundles(document.getUri(),textualItem.getUri()));
                LOG.info("New (textual) Item: " + textualItem.getUri() + " from Document: " + document.getUri());
            }

            // -> Image Item

            // -> Workflow Item

        }catch (Exception e){
            LOG.error("Error adding item from: " + resource.getUri(), e);
        }

    }


    private Item createItem(String content, String type, String url, Language language){
        Item item = Resource.newItem(content);
        item.setUri(uriGenerator.basedOnContent(Resource.Type.ITEM,content));
        item.setFormat(type);
        item.setUrl(url);
        String tokens = tokenizer.tokenize(item.getContent(),language).stream().
                filter(token -> token.isValid()).
                map(token -> token.getLemma()).
                collect(Collectors.joining(" "));
        item.setTokens(tokens);
        return item;
    }
}
