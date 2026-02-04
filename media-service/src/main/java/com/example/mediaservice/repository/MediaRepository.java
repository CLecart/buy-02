package com.example.mediaservice.repository;

import com.example.mediaservice.model.MediaFile;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Optional repository to store media file metadata. Injection is optional so the service can run
 * without MongoDB during lightweight tests.
 */
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaRepository extends MongoRepository<MediaFile, String> {

	List<MediaFile> findByProductId(String productId);

	List<MediaFile> findByOwnerId(String ownerId);

	Page<MediaFile> findByProductId(String productId, Pageable pageable);

	Page<MediaFile> findByOwnerId(String ownerId, Pageable pageable);
}
