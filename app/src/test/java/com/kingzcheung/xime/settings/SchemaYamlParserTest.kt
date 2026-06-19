package com.kingzcheung.xime.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SchemaYamlParserTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private fun writeSchema(content: String): File {
        val f = tempDir.newFile("test_schema.schema.yaml")
        f.writeText(content)
        return f
    }

    @Test
    fun `standard schema name`() {
        val f = writeSchema("""
            schema:
              schema_id: luna_pinyin
              name: 朙月拼音
              version: "0.23"
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("luna_pinyin", meta?.schemaId)
        assertEquals("朙月拼音", meta?.name)
    }

    @Test
    fun `name empty falls back to schema id`() {
        val f = writeSchema("""
            schema:
              schema_id: test_schema
              name: ""
              version: "1.0"
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("test_schema", meta?.schemaId)
        assertEquals("test_schema", meta?.name)
    }

    @Test
    fun `author as scalar`() {
        val f = writeSchema("""
            schema:
              schema_id: test_schema
              name: Test Schema
              author: "Single Author"
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("Single Author", meta?.author)
    }

    @Test
    fun `author as list takes first item`() {
        val f = writeSchema("""
            schema:
              schema_id: test_schema
              name: Test Schema
              author:
                - "Author One"
                - "Author Two"
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("Author One", meta?.author)
    }

    @Test
    fun `description block scalar`() {
        val f = writeSchema("""
            schema:
              schema_id: test_schema
              name: Test Schema
              description: |
                First line
                Second line
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("First line\nSecond line", meta?.description)
    }

    @Test
    fun `extra fields are ignored`() {
        val f = writeSchema("""
            schema:
              schema_id: test_schema
              name: Test Schema
              unknown_field: should_not_crash
            switches:
              - name: ascii_mode
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("test_schema", meta?.schemaId)
        assertEquals("Test Schema", meta?.name)
    }

    @Test
    fun `version with comments before schema block`() {
        val f = writeSchema("""
            # Rime schema
            # encoding: utf-8
            schema:
              schema_id: wubi86
              name: 五笔86版
              version: "1.0"
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("wubi86", meta?.schemaId)
        assertEquals("五笔86版", meta?.name)
    }

    @Test
    fun `patch section contains author field does not override schema name`() {
        val f = writeSchema("""
            schema:
              schema_id: test_schema
              name: Real Name
            patch:
              schema/name: Patched Name
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertEquals("Real Name", meta?.name)
    }

    @Test
    fun `file with no schema block returns null`() {
        val f = writeSchema("""
            switches:
              - name: ascii_mode
        """.trimIndent())
        val meta = SchemaManager.parseSchemaYaml(f)
        assertNull(meta)
    }
}
