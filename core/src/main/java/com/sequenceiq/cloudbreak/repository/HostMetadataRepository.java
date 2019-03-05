package com.sequenceiq.cloudbreak.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.sequenceiq.cloudbreak.domain.stack.cluster.host.HostMetadata;

@EntityType(entityClass = HostMetadata.class)
public interface HostMetadataRepository extends CrudRepository<HostMetadata, Long> {

    @Query("SELECT h FROM HostMetadata h "
            + "WHERE h.id IN (SELECT max(id) FROM HostMetadata WHERE h.hostGroup.cluster.id= :clusterId GROUP BY h.hostName)")
    Set<HostMetadata> findHostsInCluster(@Param("clusterId") Long clusterId);

    @Query("SELECT h FROM HostMetadata h "
            + "WHERE h.hostGroup.id= :hostGroupId AND (h.hostMetadataState= 'CONTAINER_RUNNING' OR h.hostMetadataState= 'SERVICES_RUNNING')")
    Set<HostMetadata> findEmptyHostsInHostGroup(@Param("hostGroupId") Long hostGroupId);

    @Query("SELECT h FROM HostMetadata h "
            + "WHERE h.id IN (SELECT max(id) FROM HostMetadata WHERE h.hostGroup.cluster.id= :clusterId AND h.hostName = :hostName)")
    HostMetadata findHostInClusterByName(@Param("clusterId") Long clusterId, @Param("hostName") String hostName);
}
