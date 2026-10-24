package de.oberamsystems.slm;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.OffsetDateTime;

import de.oberamsystems.slm.config.TimeZoneConfig;
import de.oberamsystems.utils.Utils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
            .parse("postgis/postgis:18-3.6")
            .asCompatibleSubstituteFor("postgres");

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGIS_IMAGE)
            .withDatabaseName("slm_test")
            .withUsername("test")
            .withPassword("test")
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema-init.sql"), "/docker-entrypoint-initdb.d/01-init.sql");

    static {
        postgres.start();
    }

    protected OffsetDateTime toOffset(int year, int month, int day, int hour, int minute) {
        return Utils.toOffset(year, month, day, hour, minute, TimeZoneConfig.getZoneId());
    }

    protected OffsetDateTime toOffset(int year, int month, int day, int hour, int minute, int second) {
        return Utils.toOffset(year, month, day, hour, minute, second, TimeZoneConfig.getZoneId());
    }
}
