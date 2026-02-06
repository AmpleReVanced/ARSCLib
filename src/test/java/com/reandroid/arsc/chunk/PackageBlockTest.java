package com.reandroid.arsc.chunk;

import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import org.junit.Assert;
import org.junit.Test;

public class PackageBlockTest {

    @Test
    public void testSetNameAndUpdateReferences() {
        // Create a table block and package
        TableBlock tableBlock = new TableBlock();
        PackageBlock packageBlock = tableBlock.newPackage(0x7f, "com.example.app");
        
        // Get the table string pool
        TableStringPool stringPool = tableBlock.getTableStringPool();
        
        // Add some test strings with resource references
        TableString refString1 = stringPool.getOrCreate("@com.example.app:color/primary");
        TableString refString2 = stringPool.getOrCreate("@com.example.app:string/app_name");
        TableString attrString = stringPool.getOrCreate("?com.example.app:attr/theme");
        TableString normalString = stringPool.getOrCreate("normal string");
        TableString otherPackageRef = stringPool.getOrCreate("@android:color/white");
        
        // Change package name and update references
        packageBlock.setNameAndUpdateReferences("com.example.app.modified");
        
        // Verify the package name was changed
        Assert.assertEquals("com.example.app.modified", packageBlock.getName());
        
        // Verify resource references were updated
        Assert.assertEquals("@com.example.app.modified:color/primary", refString1.get());
        Assert.assertEquals("@com.example.app.modified:string/app_name", refString2.get());
        Assert.assertEquals("?com.example.app.modified:attr/theme", attrString.get());
        
        // Verify other strings were not modified
        Assert.assertEquals("normal string", normalString.get());
        Assert.assertEquals("@android:color/white", otherPackageRef.get());
    }

    @Test
    public void testSetNameAndUpdateReferences_NoTableBlock() {
        // Test when package is not attached to a table block
        PackageBlock packageBlock = new PackageBlock();
        packageBlock.setId(0x7f);
        packageBlock.setName("com.test");
        
        // Should not throw exception
        packageBlock.setNameAndUpdateReferences("com.test.new");
        Assert.assertEquals("com.test.new", packageBlock.getName());
    }

    @Test
    public void testSetNameAndUpdateReferences_SameName() {
        TableBlock tableBlock = new TableBlock();
        PackageBlock packageBlock = tableBlock.newPackage(0x7f, "com.example");
        
        TableStringPool stringPool = tableBlock.getTableStringPool();
        TableString refString = stringPool.getOrCreate("@com.example:color/test");
        
        // Set to the same name - should not change anything
        packageBlock.setNameAndUpdateReferences("com.example");
        
        Assert.assertEquals("com.example", packageBlock.getName());
        Assert.assertEquals("@com.example:color/test", refString.get());
    }

    @Test
    public void testPackageNameAliasResolution() {
        TableBlock tableBlock = new TableBlock();
        PackageBlock packageBlock = tableBlock.newPackage(0x7f, "com.example.new");
        
        // Create a resource
        packageBlock.getOrCreate("", "string", "test_string")
                .setValueAsString("Hello World");
        
        // Register alias: old name -> new name
        tableBlock.addPackageNameAlias("com.example.old", "com.example.new");
        
        // Try to get resource using the old package name
        com.reandroid.arsc.model.ResourceEntry resource = 
                tableBlock.getResource("com.example.old", "string", "test_string");
        
        // Should find the resource through alias
        Assert.assertNotNull("Resource should be found through alias", resource);
        Assert.assertEquals("test_string", resource.getName());
        
        // Also test with the new name
        com.reandroid.arsc.model.ResourceEntry resource2 = 
                tableBlock.getResource("com.example.new", "string", "test_string");
        Assert.assertNotNull("Resource should be found with actual name", resource2);
        Assert.assertEquals(resource.getResourceId(), resource2.getResourceId());
    }

    @Test
    public void testIsResourceId() {
        Assert.assertFalse(PackageBlock.isResourceId(0x7f00ffff));
        Assert.assertFalse(PackageBlock.isResourceId(0x00ff0000));
        Assert.assertFalse(PackageBlock.isResourceId(0x00ffffff));
        Assert.assertFalse(PackageBlock.isResourceId(0x0000ffff));
        Assert.assertFalse(PackageBlock.isResourceId(0xff000000));
        Assert.assertFalse(PackageBlock.isResourceId(0x0));
        Assert.assertTrue(PackageBlock.isResourceId(0x0101ffff));
        Assert.assertTrue(PackageBlock.isResourceId(0x01010000));
    }

    @Test
    public void testIsPackageId() {
        Assert.assertFalse(PackageBlock.isPackageId(0x00));
        Assert.assertFalse(PackageBlock.isPackageId(0xfff));
        Assert.assertFalse(PackageBlock.isPackageId(0xffff));
        Assert.assertFalse(PackageBlock.isPackageId(0xfffff));
        Assert.assertFalse(PackageBlock.isPackageId(0xffffff));
        Assert.assertFalse(PackageBlock.isPackageId(0xffffffff));
        Assert.assertTrue(PackageBlock.isPackageId(0x01));
        Assert.assertTrue(PackageBlock.isPackageId(0x11));
        Assert.assertTrue(PackageBlock.isPackageId(0xff));
    }

}
