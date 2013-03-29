package cz.cvut.authserver.oauth2.dao.mongo.support;

import com.google.common.io.Files;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class MongoSeedLoader {

    private static final Logger LOG = LoggerFactory.getLogger(MongoSeedLoader.class);
    private static final Charset ENCODING = Charset.forName("UTF-8");

    private MongoOperations mongo;
    private File location;
    private boolean dropAll = false;
    private boolean skipNonEmptyCollections = true;


    @SuppressWarnings("unchecked")
    public void seed() throws IOException {
        LOG.info("Loading seed data into database...");

        if (dropAll) dropAll();

        String content = Files.toString(location, ENCODING);
        Object parsed = JSON.parse(content);

        Assert.state(parsed instanceof DBObject,
                "Parsed object must be instance of DBObject");
        DBObject dbObject = (DBObject) parsed;

        for (String collectionName : dbObject.keySet()) {
            DBCollection collection = mongo.getCollection(collectionName);

            if (skipNonEmptyCollections && collection.count() != 0) {
                LOG.info("Collection {} is not empty, skipping seed", collectionName);
                continue;
            }
            LOG.info("Seeding collection: {}", collectionName);
            Object entries = dbObject.get(collectionName);

            Assert.state(entries instanceof List,
                    "There must be list of entries under the collection key");
            collection.insert((List<DBObject>) entries);
        }
    }

    protected void dropAll() {
        for (String name : mongo.getCollectionNames()) {
            if (name.startsWith("system")) continue;

            LOG.warn("Dropping collection: {}", name);
            mongo.dropCollection(name);
        }
    }


    /**
     * Location of JSON file to import that must follow this structure:
     *
     * <pre>
     *   {
     *     "first_collection_name": [
     *       { entry_1 },
     *       { entry_2 },
     *       ...
     *     ],
     *     "second_collection_name": [
     *       ...
     *     ]
     *   }
     * </pre>
     *
     * @param location JSON file
     */
    @Required
    public void setLocation(Resource resource) {
        try {
            this.location = resource.getFile();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Required
    public void setMongoTemplate(MongoOperations mongoTemplate) {
        this.mongo = mongoTemplate;
    }

    /**
     * Drop all collections before seeding? Default is <tt>false</tt>.
     *
     * @param dropAll
     */
    public void setDropAll(boolean dropAll) {
        this.dropAll = dropAll;
    }

    /**
     * Skip import for collections that already exists? Default is <tt>true</tt>.
     *
     * @param skipNonEmptyCollections
     */
    public void setSkipNonEmptyCollections(boolean skipNonEmptyCollections) {
        this.skipNonEmptyCollections = skipNonEmptyCollections;
    }
}