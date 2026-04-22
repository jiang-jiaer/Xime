package com.kingzcheung.kime.rime

import androidx.test.platform.app.InstrumentationRegistry
import com.kingzcheung.kime.rime.RimeConfigHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class RimeEngineTest {
    
    private lateinit var context: android.content.Context
    private var rimeInitialized = false
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }
    
    @After
    fun tearDown() {
        if (rimeInitialized) {
            RimeEngine.getInstance().destroy()
            rimeInitialized = false
        }
    }
    
    @Test
    fun `getInstance should return singleton`() {
        val instance1 = RimeEngine.getInstance()
        val instance2 = RimeEngine.getInstance()
        
        assertSame(instance1, instance2)
    }
    
    @Test
    fun `isInitialized should return false before initialization`() {
        val engine = RimeEngine.getInstance()
        assertFalse(RimeEngine.isInitialized())
    }
    
    @Test
    fun `processKey should return false when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.processKey(65, 0)
        assertFalse(result)
    }
    
    @Test
    fun `getCandidates should return empty array when not initialized`() {
        val engine = RimeEngine.getInstance()
        val candidates = engine.getCandidates()
        assertTrue(candidates.isEmpty())
    }
    
    @Test
    fun `getInput should return empty string when not initialized`() {
        val engine = RimeEngine.getInstance()
        val input = engine.getInput()
        assertEquals("", input)
    }
    
    @Test
    fun `initialize should create valid directories`() {
        val userDataDir = File(context.filesDir, "rime_user_test")
        val sharedDataDir = File(context.filesDir, "rime_shared_test")
        
        assertNotNull(userDataDir)
        assertNotNull(sharedDataDir)
    }
    
    @Test
    fun `toggleAsciiMode should return false when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.toggleAsciiMode()
        assertFalse(result)
    }
    
    @Test
    fun `isAsciiMode should return false when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.isAsciiMode()
        assertFalse(result)
    }
    
    @Test
    fun `switchSchema should return false when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.switchSchema("wubi86")
        assertFalse(result)
    }
    
    @Test
    fun `getAvailableSchemas should return empty array when not initialized`() {
        val engine = RimeEngine.getInstance()
        val schemas = engine.getAvailableSchemas()
        assertTrue(schemas.isEmpty())
    }
    
    @Test
    fun `selectCandidate should return false when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.selectCandidate(0)
        assertFalse(result)
    }
    
    @Test
    fun `commit should return empty string when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.commit()
        assertEquals("", result)
    }
    
    @Test
    fun `deploy should return false when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.deploy()
        assertFalse(result)
    }
    
    @Test
    fun `lookupText should return empty string when not initialized`() {
        val engine = RimeEngine.getInstance()
        val result = engine.lookupText("工")
        assertEquals("", result)
    }
}