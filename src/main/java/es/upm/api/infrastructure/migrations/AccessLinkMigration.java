package es.upm.api.infrastructure.migrations;

import com.mongodb.client.MongoCollection;
import es.upm.api.data.entities.AccessLink;
import es.upm.api.infrastructure.support.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
@Profile("prod")
public class AccessLinkMigration implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final HashService hashService;

    @Override
    public void run(ApplicationArguments args) {
        int migratedAccessLinks = 0;
        String collectionName = this.mongoTemplate.getCollectionName(AccessLink.class);
        MongoCollection<Document> collection = this.mongoTemplate.getCollection(collectionName);
        for (Document accessLink : collection.find()) {
            Object id = accessLink.get("_id");
            if (!(id instanceof String oldId)) {
                continue;
            }
            Document newAccessLink = new Document();
            newAccessLink.put("_id", UUID.randomUUID());
            newAccessLink.put("urlId", oldId);
            newAccessLink.put("token", this.hashService.hash(oldId));
            newAccessLink.put("user", accessLink.get("user"));
            newAccessLink.put("createdAt", accessLink.get("createdAt"));
            newAccessLink.put("lastUsedAt", accessLink.get("lastUsedForUpdatedAt"));
            newAccessLink.put("expiresAt", accessLink.get("expiresAt"));
            newAccessLink.put("remainingUses", accessLink.get("remainingUses"));
            newAccessLink.put("scope", accessLink.get("scope"));
            newAccessLink.put("documentId", accessLink.get("document"));
            collection.insertOne(newAccessLink);
            collection.deleteOne(new Document("_id", oldId));
            migratedAccessLinks++;
        }
        log.warn("AccessLink migration finished. Migrated access links: {}", migratedAccessLinks);
    }
}
