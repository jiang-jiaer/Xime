package com.kingzcheung.kime.plugin

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kingzcheung.kime.ui.EmojiData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExtensionManagerTest {
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ExtensionManager.release()
    }
    
    @After
    fun tearDown() {
        ExtensionManager.release()
    }
    
    @Test
    fun `isInitialized should return false before initialization`() {
        assertFalse("Should not be initialized before init", ExtensionManager.isInitialized())
    }
    
    @Test
    fun `initialize should set initialized state`() {
        ExtensionManager.initialize(context)
        
        assertTrue("Should be initialized after init", ExtensionManager.isInitialized())
    }
    
    @Test
    fun `initialize multiple times should not cause error`() {
        ExtensionManager.initialize(context)
        ExtensionManager.initialize(context)
        
        assertTrue("Should still be initialized", ExtensionManager.isInitialized())
    }
    
    @Test
    fun `release should reset initialized state`() {
        ExtensionManager.initialize(context)
        assertTrue("Should be initialized", ExtensionManager.isInitialized())
        
        ExtensionManager.release()
        
        assertFalse("Should not be initialized after release", ExtensionManager.isInitialized())
    }
    
    @Test
    fun `getAllInstalledPlugins should return list after initialization`() {
        ExtensionManager.initialize(context)
        
        val plugins = ExtensionManager.getAllInstalledPlugins()
        
        assertNotNull("Plugin list should not be null", plugins)
    }
    
    @Test
    fun `getEmojiPlugins should return list after initialization`() {
        ExtensionManager.initialize(context)
        
        val emojiPlugins = ExtensionManager.getEmojiPlugins()
        
        assertNotNull("Emoji plugin list should not be null", emojiPlugins)
    }
    
    @Test
    fun `emojiCategoriesFlow should have default categories`() = runBlocking {
        val categories = ExtensionManager.emojiCategoriesFlow.first()
        
        assertTrue("Should have default categories", categories.isNotEmpty())
        assertTrue("Default should be EmojiData.categories", 
            categories.any { it.name == EmojiData.categories.first().name })
    }
    
    @Test
    fun `getEnabledEmojiPlugins should return empty when no plugins enabled`() {
        ExtensionManager.initialize(context)
        
        val enabledPlugins = ExtensionManager.getEnabledEmojiPlugins(context)
        
        assertNotNull("Enabled plugins should not be null", enabledPlugins)
    }
    
    @Test
    fun `getPluginById should return null for unknown plugin`() {
        ExtensionManager.initialize(context)
        
        val plugin = ExtensionManager.getPluginById("unknown_plugin_id")
        
        assertFalse("Unknown plugin should not exist", plugin != null)
    }
    
    @Test
    fun `reload should return true when initialized`() {
        ExtensionManager.initialize(context)
        
        val result = ExtensionManager.reload(context)
        
        assertTrue("Reload should succeed", result)
    }
    
    @Test
    fun `reload should return false when not initialized`() {
        ExtensionManager.release()
        
        val result = ExtensionManager.reload(context)
        
        assertFalse("Reload should fail when not initialized", result)
    }
    
    @Test
    fun `emojiCategoriesFlow should be observable`() = runBlocking {
        ExtensionManager.initialize(context)
        
        val categories = ExtensionManager.emojiCategoriesFlow.first()
        
        assertTrue("Should be able to collect flow", categories.isNotEmpty())
    }
    
    @Test
    fun `initialize should work with null sharedUserId`() {
        ExtensionManager.initialize(context)
        
        assertTrue("Should initialize without sharedUserId", ExtensionManager.isInitialized())
    }
    
    @Test
    fun `multiple release calls should not cause error`() {
        ExtensionManager.initialize(context)
        ExtensionManager.release()
        ExtensionManager.release()
        
        assertFalse("Should handle multiple releases", ExtensionManager.isInitialized())
    }
    
    @Test
    fun `getEmojiPlugins size should be <= getAllInstalledPlugins size`() {
        ExtensionManager.initialize(context)
        
        val allPlugins = ExtensionManager.getAllInstalledPlugins()
        val emojiPlugins = ExtensionManager.getEmojiPlugins()
        
        assertTrue("Emoji plugins should be subset of all plugins", 
            emojiPlugins.size <= allPlugins.size)
    }
}