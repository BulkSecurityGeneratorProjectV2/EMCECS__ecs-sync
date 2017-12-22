/*
 * Copyright 2013-2017 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.ecs.sync.model;

import com.emc.atmos.api.bean.Permission;
import com.emc.ecs.sync.util.Iso8601Util;
import com.emc.ecs.sync.util.MultiValueMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class ObjectMetadataTest {
    @Test
    public void testMultiValueMap() {
        MultiValueMap<String, String> one = new MultiValueMap<>();
        MultiValueMap<String, String> two = new MultiValueMap<>();

        // test same keys, different values
        one.add("foo", "bar");
        two.add("foo", null);
        one.add("bar", "baz");
        two.add("bar", "foo");
        Assert.assertNotEquals(one, two);

        // test after update
        two.putSingle("bar", "baz");
        two.putSingle("foo", "bar");
        Assert.assertEquals(one, two);

        // test extra key
        two.add("oh", "yeah");
        Assert.assertNotEquals(one, two);

        // test after delete
        two.remove("oh");
        Assert.assertEquals(one, two);

        // test different sized value lists
        two.add("foo", "yo!");
        Assert.assertNotEquals(one, two);
    }

    @Test
    public void testJsonSerialization() {
        ObjectAcl acl = new ObjectAcl();
        acl.setOwner("stu");
        acl.addGroupGrant("other", Permission.READ.toString());
        acl.addUserGrant("stu", Permission.FULL_CONTROL.toString());
        acl.addUserGrant("jason", Permission.NONE.toString());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(210881);
        metadata.setChecksum(new Checksum("MD5", "xxyyxxyy"));
        metadata.setContentType("application/ms-excel");
        metadata.setModificationTime(new Date());
        metadata.setAccessTime(new Date());
        metadata.setMetaChangeTime(new Date());
        metadata.setExpirationDate(new Date());

        metadata.setUserMetadataValue("foo", "bar");
        metadata.setUserMetadataValue("baz", null);
        metadata.setUserMetadataValue("crazy", " b|1+NB8o6%g HE@dIfAxm  ");

        String jsonString = metadata.toJson();

        ObjectMetadata mFromJson = ObjectMetadata.fromJson(jsonString);
        Assert.assertEquals(metadata.getContentLength(), mFromJson.getContentLength());
        Assert.assertEquals(metadata.getUserMetadata(), mFromJson.getUserMetadata());
        Assert.assertEquals(metadata.getContentType(), mFromJson.getContentType());
        Assert.assertEquals(metadata.getChecksum(), mFromJson.getChecksum());
        Assert.assertEquals(Iso8601Util.format(metadata.getModificationTime()),
                Iso8601Util.format(mFromJson.getModificationTime()));
        Assert.assertEquals(Iso8601Util.format(metadata.getAccessTime()),
                Iso8601Util.format(mFromJson.getAccessTime()));
        Assert.assertEquals(Iso8601Util.format(metadata.getMetaChangeTime()),
                Iso8601Util.format(mFromJson.getMetaChangeTime()));
        Assert.assertEquals(Iso8601Util.format(metadata.getExpirationDate()),
                Iso8601Util.format(mFromJson.getExpirationDate()));
    }

    @Test
    public void testPathTranslation() {
        String sourcePath = "/foo/bar/baz.zip/dir";
        String metaPath = ObjectMetadata.getMetaPath(sourcePath, true);
        Assert.assertEquals("dir failed", "/foo/bar/baz.zip/dir/" + ObjectMetadata.METADATA_DIR + "/" + ObjectMetadata.DIR_META_FILE + "", metaPath);

        sourcePath = "/foo/bar/baz.zip/dir/object";
        metaPath = ObjectMetadata.getMetaPath(sourcePath, false);
        Assert.assertEquals("object failed", "/foo/bar/baz.zip/dir/" + ObjectMetadata.METADATA_DIR + "/object", metaPath);

        sourcePath = "foo/bar/baz/object";
        metaPath = ObjectMetadata.getMetaPath(sourcePath, false);
        Assert.assertEquals("relative path failed", "foo/bar/baz/" + ObjectMetadata.METADATA_DIR + "/object", metaPath);
    }

    @Test
    public void testSyncAclClone() throws Exception {
        ObjectAcl one = new ObjectAcl();
        one.setOwner("bob");
        one.addUserGrant("bob", "all");
        one.addGroupGrant("people", "read");

        ObjectAcl two = (ObjectAcl) one.clone();
        two.setOwner("john");
        two.addUserGrant("bob", "acl");
        two.addGroupGrant("everyone", "read");

        Assert.assertFalse(one == two);
        Assert.assertFalse(one.getUserGrants() == two.getUserGrants());
        Assert.assertFalse(one.getGroupGrants() == two.getGroupGrants());
        Assert.assertFalse(one.getUserGrants().get("bob") == two.getUserGrants().get("bob"));
        Assert.assertEquals("bob", one.getOwner());
        Assert.assertEquals(1, one.getUserGrants().get("bob").size());
        Assert.assertEquals(2, two.getUserGrants().get("bob").size());
        Assert.assertEquals(1, one.getGroupGrants().size());
        Assert.assertEquals(2, two.getGroupGrants().size());
    }
}
