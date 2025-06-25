package io.github.admiralxy.cache.jpa.api.entity.leader;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "t_jpa_cache_leader")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaCacheLeaderEntity {

    /**
     * Identifier.
     */
    @Id
    @Column(name = "c_id", nullable = false)
    private int id;

    /**
     * Instance identifier of the leader.
     */
    @Column(name = "c_instance_id")
    private String instanceId;

    /**
     * Leader lease expiration time.
     */
    @Column(name = "c_lease_until")
    private Instant leaseUntil;
}
