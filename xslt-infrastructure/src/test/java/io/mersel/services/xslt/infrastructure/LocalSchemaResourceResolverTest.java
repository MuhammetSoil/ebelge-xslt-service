package io.mersel.services.xslt.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.ls.LSInput;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LocalSchemaResourceResolver")
class LocalSchemaResourceResolverTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("goreceli_import_ek_arama_dizininden_cozulur")
    void goreceli_import_ortak_dizinden_cozulur() throws Exception {
        Path specializedDir = Files.createDirectories(tempDir.resolve("edoviz"));
        Path sharedDir = Files.createDirectories(tempDir.resolve("earchive"));
        Path sharedSchema = sharedDir.resolve("XAdES.xsd");
        Files.writeString(sharedSchema, "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>");

        var resolver = new LocalSchemaResourceResolver(specializedDir, sharedDir);
        LSInput resolved = resolver.resolveResource(
                "http://www.w3.org/2001/XMLSchema",
                "http://uri.etsi.org/01903/v1.3.2#",
                null,
                "XAdES.xsd",
                specializedDir.resolve("eArsiv.xsd").toUri().toString());

        assertThat(resolved).isNotNull();
        assertThat(resolved.getSystemId()).isEqualTo(sharedSchema.toUri().toString());
        try (var stream = resolved.getByteStream()) {
            assertThat(stream).isNotNull();
        }
    }

    @Test
    @DisplayName("goreceli_import_once_base_uri_ile_cozulur — aynı adlı başka XSD seçilmez")
    void goreceli_import_once_base_uri_ile_cozulur() throws Exception {
        Path schemaRoot = Files.createDirectories(tempDir.resolve("schema"));
        Path importingDir = Files.createDirectories(schemaRoot.resolve("reports/current"));
        Path expected = Files.createDirectories(importingDir.resolve("common")).resolve("types.xsd");
        Path wrong = Files.createDirectories(schemaRoot.resolve("other/common")).resolve("types.xsd");
        Files.writeString(expected, "expected");
        Files.writeString(wrong, "wrong");

        var resolver = new LocalSchemaResourceResolver(schemaRoot);
        LSInput resolved = resolver.resolveResource(
                "http://www.w3.org/2001/XMLSchema", null, null,
                "common/types.xsd", importingDir.resolve("main.xsd").toUri().toString());

        assertThat(resolved).isNotNull();
        assertThat(resolved.getSystemId()).isEqualTo(expected.toUri().toString());
    }

    @Test
    @DisplayName("ust_dizine_goreceli_import_base_uri_ile_cozulur")
    void ust_dizine_goreceli_import_base_uri_ile_cozulur() throws Exception {
        Path schemaRoot = Files.createDirectories(tempDir.resolve("schema"));
        Path importingDir = Files.createDirectories(schemaRoot.resolve("maindoc"));
        Path expected = Files.createDirectories(schemaRoot.resolve("common")).resolve("types.xsd");
        Files.writeString(expected, "expected");

        var resolver = new LocalSchemaResourceResolver(importingDir);
        LSInput resolved = resolver.resolveResource(
                "http://www.w3.org/2001/XMLSchema", null, null,
                "../common/types.xsd", importingDir.resolve("main.xsd").toUri().toString());

        assertThat(resolved).isNotNull();
        assertThat(resolved.getSystemId()).isEqualTo(expected.toUri().toString());
    }

    @Test
    @DisplayName("goreceli_import_tam_suffix_ile_cozulur — yalnızca dosya adı kullanılmaz")
    void goreceli_import_tam_suffix_ile_cozulur() throws Exception {
        Path primaryDir = Files.createDirectories(tempDir.resolve("primary"));
        Path sharedDir = Files.createDirectories(tempDir.resolve("shared"));
        Path wrong = Files.createDirectories(sharedDir.resolve("legacy")).resolve("types.xsd");
        Path expected = Files.createDirectories(sharedDir.resolve("common")).resolve("types.xsd");
        Files.writeString(wrong, "wrong");
        Files.writeString(expected, "expected");

        var resolver = new LocalSchemaResourceResolver(primaryDir, sharedDir);
        LSInput resolved = resolver.resolveResource(
                "http://www.w3.org/2001/XMLSchema", null, null,
                "common/types.xsd", primaryDir.resolve("main.xsd").toUri().toString());

        assertThat(resolved).isNotNull();
        assertThat(resolved.getSystemId()).isEqualTo(expected.toUri().toString());
    }
}
